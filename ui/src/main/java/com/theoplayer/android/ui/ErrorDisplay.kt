package com.theoplayer.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

@Composable
fun ErrorDisplay(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = contentColorFor(color),
    shape: Shape = RectangleShape,
) {
    val error = LocalTHEOplayer.current?.error

    error?.let {
        Surface(
            modifier = modifier,
            color = color,
            contentColor = contentColor,
            shape = shape,
        ) {
            Text(
                color = contentColor,
                text = "${it.message}"
            )
        }
    }
}
