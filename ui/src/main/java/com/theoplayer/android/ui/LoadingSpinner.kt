package com.theoplayer.android.ui

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
) {
    val state = LocalTHEOplayer.current
    if (state?.loading == true) {
        CircularProgressIndicator(modifier = modifier, color = color)
    }
}
