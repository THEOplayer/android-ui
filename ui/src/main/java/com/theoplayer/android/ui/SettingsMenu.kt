package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MenuScope.SettingsMenu() {
    Menu(
        title = {
            Text(color = Color.White, text = "Settings")
        }
    ) {
        val state = LocalTHEOplayer.current
        TextButton(onClick = { openMenu { QualityMenu() } }) {
            Text(
                color = Color.White,
                text = "Active quality: ${state?.activeVideoQuality?.height?.let { "${it}p" } ?: "none"}"
            )
        }
    }
}

@Composable
fun MenuScope.QualityMenu() {
    Menu(
        title = {
            Text(color = Color.White, text = "Quality")
        }
    ) {
        QualityList(onClick = { closeCurrentMenu() })
    }
}
