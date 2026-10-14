package com.theoplayer.android.ui.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import com.theoplayer.android.ui.tv.ExperimentalTvUiApi
import com.theoplayer.android.ui.theme.LocalPlayerAnimations
import com.theoplayer.android.ui.theme.PlayerAnimations
import kotlin.time.Duration.Companion.seconds

/**
 * Provides TV Material theme values and animations to a player UI.
 *
 * @param playerAnimations animation settings for the TV player UI
 */
@ExperimentalTvUiApi
@Composable
fun THEOplayerTvTheme(
    colorScheme: ColorScheme = darkColorScheme(),
    typography: Typography = MaterialTheme.typography,
    playerAnimations: PlayerAnimations = PlayerAnimations(controlsHideDelay = 5.seconds),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPlayerAnimations provides playerAnimations) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
