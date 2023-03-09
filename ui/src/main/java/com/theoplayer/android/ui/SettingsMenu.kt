package com.theoplayer.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MenuScope.SettingsMenu() {
    Menu(
        title = {
            Text(color = Color.White, text = "Settings")
        },
        backIcon = {
            Icon(
                Icons.Rounded.Close,
                tint = Color.White,
                contentDescription = "Close"
            )
        },
    ) {
        val state = LocalTHEOplayer.current
        TextButton(onClick = { openMenu { QualityMenu() } }) {
            Text(
                color = Color.White,
                text = "Active quality: ${state?.activeVideoQuality?.height?.let { "${it}p" } ?: "none"}"
            )
        }
        TextButton(onClick = { openMenu { PlaybackRateMenu() } }) {
            Text(
                color = Color.White,
                text = "Playback speed: ${formatPlaybackRate(state?.playbackRate ?: 1.0)}"
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

@Composable
fun MenuScope.PlaybackRateMenu() {
    Menu(
        title = {
            Text(color = Color.White, text = "Playback speed")
        }
    ) {
        PlaybackRateList(onClick = { closeCurrentMenu() })
    }
}
