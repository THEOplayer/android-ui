package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ErrorDisplay(
    modifier: Modifier = Modifier,
) {
    val error = LocalTHEOplayer.current?.error

    error?.let { it ->
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Icon(
                    Icons.Rounded.Error,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
                    contentDescription = null
                )
            }
            Column {
                Text(
                    text = "An error occurred",
                    style = MaterialTheme.typography.headlineMedium,
                    lineHeight = 48.sp
                )
                Text(
                    text = "${it.message}"
                )
            }
        }
    }
}
