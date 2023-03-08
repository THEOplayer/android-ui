package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MenuScope.SettingsMenu() {
    Menu(
        title = {
            Text(color = Color.White, text = "Settings")
        }
    ) {
        Text(color = Color.White, text = "Hello world!")
    }
}