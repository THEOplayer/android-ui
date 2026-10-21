package com.theoplayer.android.ui.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.theme.LocalPlayerAnimations
import com.theoplayer.android.ui.theme.LocalPlayerColors
import com.theoplayer.android.ui.theme.PlayerAnimations
import com.theoplayer.android.ui.theme.PlayerColors
import com.theoplayer.android.ui.theme.md_theme_dark_background
import com.theoplayer.android.ui.theme.md_theme_dark_error
import com.theoplayer.android.ui.theme.md_theme_dark_errorContainer
import com.theoplayer.android.ui.theme.md_theme_dark_inverseOnSurface
import com.theoplayer.android.ui.theme.md_theme_dark_inversePrimary
import com.theoplayer.android.ui.theme.md_theme_dark_inverseSurface
import com.theoplayer.android.ui.theme.md_theme_dark_onBackground
import com.theoplayer.android.ui.theme.md_theme_dark_onError
import com.theoplayer.android.ui.theme.md_theme_dark_onErrorContainer
import com.theoplayer.android.ui.theme.md_theme_dark_onPrimary
import com.theoplayer.android.ui.theme.md_theme_dark_onPrimaryContainer
import com.theoplayer.android.ui.theme.md_theme_dark_onSecondary
import com.theoplayer.android.ui.theme.md_theme_dark_onSecondaryContainer
import com.theoplayer.android.ui.theme.md_theme_dark_onSurface
import com.theoplayer.android.ui.theme.md_theme_dark_onSurfaceVariant
import com.theoplayer.android.ui.theme.md_theme_dark_onTertiary
import com.theoplayer.android.ui.theme.md_theme_dark_onTertiaryContainer
import com.theoplayer.android.ui.theme.md_theme_dark_outline
import com.theoplayer.android.ui.theme.md_theme_dark_outlineVariant
import com.theoplayer.android.ui.theme.md_theme_dark_primary
import com.theoplayer.android.ui.theme.md_theme_dark_primaryContainer
import com.theoplayer.android.ui.theme.md_theme_dark_scrim
import com.theoplayer.android.ui.theme.md_theme_dark_secondary
import com.theoplayer.android.ui.theme.md_theme_dark_secondaryContainer
import com.theoplayer.android.ui.theme.md_theme_dark_surface
import com.theoplayer.android.ui.theme.md_theme_dark_surfaceVariant
import com.theoplayer.android.ui.theme.md_theme_dark_tertiary
import com.theoplayer.android.ui.theme.md_theme_dark_tertiaryContainer
import com.theoplayer.android.ui.tv.ExperimentalTvUiApi
import kotlin.time.Duration.Companion.seconds

private val THEOplayerDarkColorScheme =
    darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        inversePrimary = md_theme_dark_inversePrimary,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        inverseSurface = md_theme_dark_inverseSurface,
        inverseOnSurface = md_theme_dark_inverseOnSurface,
        error = md_theme_dark_error,
        onError = md_theme_dark_onError,
        errorContainer = md_theme_dark_errorContainer,
        onErrorContainer = md_theme_dark_onErrorContainer,
        border = md_theme_dark_outline,
        borderVariant = md_theme_dark_outlineVariant,
        scrim = md_theme_dark_scrim,
    )

/**
 * Provides THEOplayer's TV color scheme, player colors and animations to a player UI.
 *
 * @param colorScheme TV Material colors for this hierarchy
 * @param typography TV Material typography for this hierarchy
 * @param playerColors additional colors used by THEOplayer UI components
 * @param playerAnimations animation settings for the TV player UI
 */
@ExperimentalTvUiApi
@OptIn(InternalUiApi::class)
@Composable
fun THEOplayerTvTheme(
    colorScheme: ColorScheme = THEOplayerDarkColorScheme,
    typography: Typography = MaterialTheme.typography,
    playerColors: PlayerColors = PlayerColors(),
    playerAnimations: PlayerAnimations = PlayerAnimations(controlsHideDelay = 5.seconds),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPlayerColors provides playerColors,
        LocalPlayerAnimations provides playerAnimations,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
