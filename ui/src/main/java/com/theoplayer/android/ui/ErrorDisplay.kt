package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ErrorDisplay() {
    val error = LocalTHEOplayer.current?.error

    error?.let {
        Text(
            color = Color.Red,
            text = "${it.message}"
        )
    }
}
