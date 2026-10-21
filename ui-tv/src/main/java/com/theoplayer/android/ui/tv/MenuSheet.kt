package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults

private val menuWidth = 320.dp
private val menuCornerRadius = 16.dp

/**
 * Draws the menu sheet on top of the player: a 320dp surface at the end edge,
 * covering the full player height.
 */
@Composable
internal fun MenuSheet(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(menuWidth)
                .testTag("menu-sheet"),
            shape = RoundedCornerShape(
                topStart = menuCornerRadius,
                bottomStart = menuCornerRadius
            ),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            content()
        }
    }
}
