package com.theoplayer.android.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * The horizontal distance the contents slide during a shared axis X transition.
 */
internal val SharedAxisSlideDistance: Dp = 30.dp

/**
 * The total duration of a shared axis X transition.
 */
internal const val SharedAxisDurationMillis: Int = 300

/**
 * Material shared axis X transition: a fade-through with a short horizontal slide,
 * as described in
 * https://m3.material.io/styles/motion/transitions/transition-patterns
 *
 * Over [durationMillis], the incoming content slides in from [slideDistance] pixels
 * (in the direction of [forward], mirrored in right-to-left layouts) while the
 * outgoing content slides the opposite way. The outgoing content fades out over the
 * first 35% of the transition and the incoming content fades in over the remaining
 * 65%. The container's size animates on the same timing, clipped to its bounds.
 *
 * @param forward whether the navigation moves forward (a push) or backward (a pop)
 * @param slideDistance the slide distance in pixels
 * @param layoutDirection the current layout direction
 * @param durationMillis the total duration of the transition
 */
internal fun sharedAxisX(
    forward: Boolean,
    slideDistance: Int,
    layoutDirection: LayoutDirection,
    durationMillis: Int = SharedAxisDurationMillis
): ContentTransform {
    val fadeOutDuration = (durationMillis * 0.35f).toInt()
    val fadeInDuration = durationMillis - fadeOutDuration
    val ltr = layoutDirection == LayoutDirection.Ltr
    // Forward: incoming starts at +slideDistance and moves to 0, outgoing moves
    // from 0 to -slideDistance. Backward is mirrored, and both mirror again in RTL.
    val incomingOffset = (if (forward) slideDistance else -slideDistance) * (if (ltr) 1 else -1)
    val outgoingOffset = -incomingOffset

    val enter = slideInHorizontally(
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
    ) { incomingOffset } + fadeIn(
        tween(fadeInDuration, delayMillis = fadeOutDuration, easing = LinearOutSlowInEasing)
    )
    val exit = slideOutHorizontally(
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
    ) { outgoingOffset } + fadeOut(
        tween(fadeOutDuration, easing = FastOutLinearInEasing)
    )

    val transform = enter togetherWith exit
    return ContentTransform(
        targetContentEnter = transform.targetContentEnter,
        initialContentExit = transform.initialContentExit,
        sizeTransform = SizeTransform(clip = true) { _, _ ->
            tween(durationMillis, easing = FastOutSlowInEasing)
        }
    )
}
