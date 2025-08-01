package com.theoplayer.android.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.isSatisfiedBy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    ConstrainedAspectRatioElement(
        ratio,
        matchHeightConstraintsFirst,
        debugInspectorInfo {
            name = "constrainedAspectRatio"
            properties["ratio"] = ratio
            properties["matchHeightConstraintsFirst"] = matchHeightConstraintsFirst
        }
    )
)

private class ConstrainedAspectRatioElement(
    val aspectRatio: Float,
    val matchHeightConstraintsFirst: Boolean,
    val inspectorInfo: InspectorInfo.() -> Unit
) : ModifierNodeElement<ConstrainedAspectRatioNode>() {
    init {
        require(aspectRatio > 0) { "aspectRatio $aspectRatio must be > 0" }
    }

    override fun create(): ConstrainedAspectRatioNode {
        return ConstrainedAspectRatioNode(
            aspectRatio,
            matchHeightConstraintsFirst
        )
    }

    override fun update(node: ConstrainedAspectRatioNode) {
        node.aspectRatio = aspectRatio
        node.matchHeightConstraintsFirst = matchHeightConstraintsFirst
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? ConstrainedAspectRatioElement ?: return false
        return aspectRatio == otherModifier.aspectRatio &&
                matchHeightConstraintsFirst == other.matchHeightConstraintsFirst
    }

    override fun hashCode(): Int =
        aspectRatio.hashCode() * 31 + matchHeightConstraintsFirst.hashCode()
}

private class ConstrainedAspectRatioNode(
    var aspectRatio: Float,
    var matchHeightConstraintsFirst: Boolean
) : LayoutModifier, Modifier.Node() {
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
}
