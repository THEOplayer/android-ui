package com.theoplayer.android.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

internal val menuSheetBreakpoint = 600.dp

private val menuSheetWidth = 400.dp

private val menuSheetMaxWidth = 640.dp

private val menuSheetMinHeight = 240.dp

private const val menuSheetAlpha = 0.9f

/**
 * Draws a menu sheet on top of the player: a click-outside scrim plus a sheet surface.
 * On compact players the sheet sits at the bottom and wraps its content, between
 * 240dp (or the full player height on players shorter than that) and 60% of the
 * player height. On wide players (at least 600dp) it is a 400dp-wide side sheet at
 * the end of the player.
 */
@Composable
internal fun MenuSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(onDismiss) {
                // Consumes taps instead of `clickable`: the scrim must not merge
                // the semantics of the sheet content into its own click action.
                detectTapGestures { onDismiss() }
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wide = maxWidth >= menuSheetBreakpoint
            val minHeight = minOf(menuSheetMinHeight, maxHeight)
            val maxSheetHeight = maxOf(maxHeight * 0.6f, minHeight)
            val fillsPlayer = !wide && minHeight >= maxHeight
            Surface(
                modifier = (
                        if (wide) {
                            Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(menuSheetWidth)
                        } else {
                            Modifier
                                .align(Alignment.BottomCenter)
                                .widthIn(max = menuSheetMaxWidth)
                                .fillMaxWidth()
                                .heightIn(min = minHeight, max = maxSheetHeight)
                        }
                        )
                    .testTag("menu-sheet")
                    .pointerInput(Unit) {
                        // Blocks taps on the sheet from reaching the scrim.
                        detectTapGestures { }
                    },
                shape = if (wide) {
                    RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                } else if (fillsPlayer) {
                    RectangleShape
                } else {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                },
                color = MaterialTheme.colorScheme.surface.copy(alpha = menuSheetAlpha),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                content()
            }
        }
    }
}
