package com.theoplayer.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MenuScope.LanguageMenuButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Language,
            tint = Color.White,
            contentDescription = "Language"
        )
    }
) {
    IconButton(
        modifier = modifier,
        onClick = { openMenu { LanguageMenu() } }) {
        content()
    }
}