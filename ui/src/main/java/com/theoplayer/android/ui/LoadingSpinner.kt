package com.theoplayer.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val state = LocalTHEOplayer.current
    val loading = state?.loading ?: false
    AnimatedVisibility(
        visible = loading,
        // Show the loading spinner only after a small delay
        enter = fadeIn(snap(delayMillis = 500)),
        // Hide the loading spinner immediately when loaded
        exit = ExitTransition.None
    ) {
        CircularProgressIndicator(modifier = modifier, color = color)
    }
}
