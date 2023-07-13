package com.theoplayer.android.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.isSatisfiedBy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

internal fun Modifier.constrainedAspectRatio(
    @FloatRange(from = 0.0, fromInclusive = false)
    ratio: Float,
    matchHeightConstraintsFirst: Boolean = false
): Modifier = this.then(
    ConstrainedAspectRatioModifier(
        ratio,
        matchHeightConstraintsFirst,
        debugInspectorInfo {
            name = "constrainedAspectRatio"
            properties["ratio"] = ratio
            properties["matchHeightConstraintsFirst"] = matchHeightConstraintsFirst
        }
    )
)

private class ConstrainedAspectRatioModifier(
    val aspectRatio: Float,
    val matchHeightConstraintsFirst: Boolean,
    inspectorInfo: InspectorInfo.() -> Unit
) : LayoutModifier, InspectorValueInfo(inspectorInfo) {
    init {
        require(aspectRatio > 0) { "aspectRatio $aspectRatio must be > 0" }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val size = constraints.findSize()
        val wrappedConstraints = if (size != IntSize.Zero) {
            Constraints.fixed(size.width, size.height)
        } else {
            constraints
        }
        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = if (height != Constraints.Infinity) {
        (height * aspectRatio).roundToInt()
    } else {
        measurable.minIntrinsicWidth(height)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = if (height != Constraints.Infinity) {
        (height * aspectRatio).roundToInt()
    } else {
        measurable.maxIntrinsicWidth(height)
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = if (width != Constraints.Infinity) {
        (width / aspectRatio).roundToInt()
    } else {
        measurable.minIntrinsicHeight(width)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = if (width != Constraints.Infinity) {
        (width / aspectRatio).roundToInt()
    } else {
        measurable.maxIntrinsicHeight(width)
    }

    private fun Constraints.findSize(): IntSize {
        if (!matchHeightConstraintsFirst) {
            tryMaxWidth().also { if (it != IntSize.Zero) return it }
            tryMaxHeight().also { if (it != IntSize.Zero) return it }
            tryMinWidth().also { if (it != IntSize.Zero) return it }
            tryMinHeight().also { if (it != IntSize.Zero) return it }
        } else {
            tryMaxHeight().also { if (it != IntSize.Zero) return it }
            tryMaxWidth().also { if (it != IntSize.Zero) return it }
            tryMinHeight().also { if (it != IntSize.Zero) return it }
            tryMinWidth().also { if (it != IntSize.Zero) return it }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMaxWidth(): IntSize {
        val maxWidth = this.maxWidth
        if (maxWidth != Constraints.Infinity) {
            val height = (maxWidth / aspectRatio).roundToInt()
            if (height > 0) {
                val size = IntSize(maxWidth, height)
                if (isSatisfiedBy(size)) {
                    return size
                }
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMaxHeight(): IntSize {
        val maxHeight = this.maxHeight
        if (maxHeight != Constraints.Infinity) {
            val width = (maxHeight * aspectRatio).roundToInt()
            if (width > 0) {
                val size = IntSize(width, maxHeight)
                if (isSatisfiedBy(size)) {
                    return size
                }
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMinWidth(): IntSize {
        val minWidth = this.minWidth
        val height = (minWidth / aspectRatio).roundToInt()
        if (height > 0) {
            val size = IntSize(minWidth, height)
            if (isSatisfiedBy(size)) {
                return size
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMinHeight(): IntSize {
        val minHeight = this.minHeight
        val width = (minHeight * aspectRatio).roundToInt()
        if (width > 0) {
            val size = IntSize(width, minHeight)
            if (isSatisfiedBy(size)) {
                return size
            }
        }
        return IntSize.Zero
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? ConstrainedAspectRatioModifier ?: return false
        return aspectRatio == otherModifier.aspectRatio &&
                matchHeightConstraintsFirst == other.matchHeightConstraintsFirst
    }

    override fun hashCode(): Int =
        aspectRatio.hashCode() * 31 + matchHeightConstraintsFirst.hashCode()

    override fun toString(): String = "ConstrainedAspectRatioModifier(aspectRatio=$aspectRatio)"
}
