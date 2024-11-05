package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A display for a fatal error, if the player encounters one.
 *
 * @param modifier the [Modifier] to be applied to this display
 * @param iconModifier the [Modifier] to be applied to the [Icon]s inside this button
 * @param iconSize the size of the icons
 */
@Composable
fun ErrorDisplay(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
) {
    Player.current?.error?.let { error ->
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Icon(
                    Icons.Rounded.Error,
                    modifier = Modifier.size(iconSize).then(iconModifier),
                    contentDescription = null
                )
            }
            Column {
                Box(
                    modifier = Modifier.defaultMinSize(minHeight = iconSize),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "An error occurred",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    text = "${error.message}"
                )
            }
        }
    }
}
