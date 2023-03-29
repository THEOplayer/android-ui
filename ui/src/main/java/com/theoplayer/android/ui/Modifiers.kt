package com.theoplayer.android.ui

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.debugInspectorInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal fun Modifier.pressable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    requireUnconsumed: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "pressable"
        properties["interactionSource"] = interactionSource
        properties["enabled"] = enabled
        properties["requireUnconsumed"] = requireUnconsumed
    }
) {
    val scope = rememberCoroutineScope()
    var pressedInteraction by remember { mutableStateOf<PressInteraction.Press?>(null) }

    suspend fun emitPress(pressPosition: Offset) {
        if (pressedInteraction == null) {
            val interaction = PressInteraction.Press(pressPosition)
            interactionSource.emit(interaction)
            pressedInteraction = interaction
        }
    }

    suspend fun emitRelease() {
        pressedInteraction?.let { oldValue ->
            val interaction = PressInteraction.Release(oldValue)
            interactionSource.emit(interaction)
            pressedInteraction = null
        }
    }

    fun tryEmitCancel() {
        pressedInteraction?.let { oldValue ->
            val interaction = PressInteraction.Cancel(oldValue)
            interactionSource.tryEmit(interaction)
            pressedInteraction = null
        }
    }

    DisposableEffect(interactionSource) {
        onDispose { tryEmitCancel() }
    }
    LaunchedEffect(enabled) {
        if (!enabled) {
            emitRelease()
        }
    }

    if (enabled) {
        Modifier
            .pointerInput(interactionSource) {
                val currentContext = currentCoroutineContext()
                awaitPointerEventScope {
                    while (currentContext.isActive) {
                        val down = awaitFirstDown(requireUnconsumed = requireUnconsumed)
                        scope.launch { emitPress(down.position) }
                        val up =
                            if (requireUnconsumed) waitForUpOrCancellation() else waitForUpOrCancellationIgnoreConsumed()
                        if (up == null) {
                            tryEmitCancel()
                        } else {
                            scope.launch { emitRelease() }
                        }
                    }
                }
            }
    } else {
        Modifier
    }
}

/**
 * Like [AwaitPointerEventScope.waitForUpOrCancellation],
 * but skips the [PointerInputChange.isConsumed] checks.
 */
private suspend fun AwaitPointerEventScope.waitForUpOrCancellationIgnoreConsumed(): PointerInputChange? {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.all { it.changedToUpIgnoreConsumed() }) {
            // All pointers are up
            return event.changes[0]
        }
        if (event.changes.any { it.isOutOfBounds(size, extendedTouchPadding) }) {
            return null // Canceled
        }
    }
}

internal fun Modifier.toggleControlsOnTap(
    controlsVisible: State<Boolean>,
    showControlsTemporarily: () -> Unit,
    hideControls: () -> Unit
): Modifier {
    return this.pointerInput(Unit) {
        coroutineScope {
            var didHideControls = false
            launch {
                detectTapGestures(onPress = {
                    didHideControls = false
                    // Hide controls immediately when pressed while visible
                    val controlsWereVisible = controlsVisible.value
                    awaitRelease()
                    if (controlsWereVisible) {
                        didHideControls = true
                        hideControls()
                    }
                })
            }
            launch {
                detectAnyPointerEvent(pass = PointerEventPass.Final) {
                    // Show controls temporarily when pressing, moving or releasing a pointer
                    // - except if we just hid the controls by pressing
                    if (didHideControls) {
                        didHideControls = false
                    } else {
                        showControlsTemporarily()
                    }
                }
            }
        }
    }
}

private suspend fun PointerInputScope.detectAnyPointerEvent(
    pass: PointerEventPass = PointerEventPass.Main,
    onPointer: () -> Unit
) {
    val currentContext = currentCoroutineContext()
    awaitPointerEventScope {
        while (currentContext.isActive) {
            awaitPointerEvent(pass = pass)
            onPointer()
        }
    }
}