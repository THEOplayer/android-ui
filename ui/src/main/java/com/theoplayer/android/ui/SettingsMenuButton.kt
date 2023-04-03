package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A button that opens the [settings menu][SettingsMenu].
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 */
@Composable
fun MenuScope.SettingsMenuButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Settings,
            contentDescription = "Settings"
        )
    }
) {
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = { openMenu { SettingsMenu() } }) {
        content()
    }
}