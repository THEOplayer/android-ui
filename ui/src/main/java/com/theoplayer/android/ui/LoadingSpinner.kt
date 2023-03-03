package com.theoplayer.android.ui

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
    if (state?.loading == true) {
        CircularProgressIndicator(modifier = modifier, color = color)
    }
}
