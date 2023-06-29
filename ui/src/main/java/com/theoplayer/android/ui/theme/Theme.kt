package com.theoplayer.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.theoplayer.android.ui.LiveButton
import com.theoplayer.android.ui.LoadingSpinner
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

/**
 * Additional colors used by THEOplayer UI components.
 */
@Immutable
data class PlayerColors(
    /**
     * The color of the circle icon of a [LiveButton] when playing at the live point.
     */
    val liveButtonLive: Color = Color.Red,
    /**
     * The color of the circle icon of a [LiveButton] when playing behind the live point.
     */
    val liveButtonDvr: Color = Color.Gray
)

private val LocalPlayerColors = staticCompositionLocalOf { PlayerColors() }

/**
 * Animation settings used by THEOplayer UI components.
 */
@Immutable
data class PlayerAnimations(
    /**
     * The animation duration when fading in the player controls.
     */
    val controlsEnterDuration: Duration = 0.milliseconds,
    /**
     * The animation duration when fading out the player controls.
     */
    val controlsExitDuration: Duration = 500.milliseconds,
    /**
     * The delay for the [LoadingSpinner] to become visible after the player starts buffering.
     */
    val loadingSpinnerDelay: Duration = 500.milliseconds,
)

private val LocalPlayerAnimations = staticCompositionLocalOf { PlayerAnimations() }

/**
 * Provides theme colors, shapes and fonts to be used by the player.
 *
 * @param colorScheme A complete definition of the Material Color theme for this hierarchy
 * @param shapes A set of corner shapes to be used as this hierarchy's shape system
 * @param typography A set of text styles to be used as this hierarchy's typography system
 * @param playerColors Additional colors used by THEOplayer UI components
 * @param playerAnimations Additional animation settings used by THEOplayer UI components
 * @see MaterialTheme
 */
@Composable
fun THEOplayerTheme(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    playerColors: PlayerColors = THEOplayerTheme.playerColors,
    playerAnimations: PlayerAnimations = THEOplayerTheme.playerAnimations,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPlayerColors provides playerColors,
        LocalPlayerAnimations provides playerAnimations
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            typography = typography,
            content = content
        )
    }
}

/**
 * Provides theme colors, shapes and fonts to be used by the player, using the THEOplayer brand style.
 *
 * @param useDarkTheme Whether to use light or dark colors.
 * By default, uses the system's dark theme setting.
 * @param shapes A set of corner shapes to be used as this hierarchy's shape system
 * @param typography A set of text styles to be used as this hierarchy's typography system
 * @param playerColors Additional colors used by THEOplayer UI components
 * @param playerAnimations Additional animation settings used by THEOplayer UI components
 * @see MaterialTheme
 */
@Composable
fun THEOplayerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    playerColors: PlayerColors = THEOplayerTheme.playerColors,
    playerAnimations: PlayerAnimations = THEOplayerTheme.playerAnimations,
    content: @Composable() () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    THEOplayerTheme(
        colorScheme = colors,
        shapes = shapes,
        typography = typography,
        playerColors = playerColors,
        playerAnimations = playerAnimations,
        content = content
    )
}

/**
 * Contains functions to access the current theme values provided at the call site's position in
 * the hierarchy.
 */
object THEOplayerTheme {
    /**
     * Retrieves the current [PlayerColors] at the call site's position in the hierarchy.
     */
    val playerColors: PlayerColors
        @Composable
        get() = LocalPlayerColors.current

    /**
     * Retrieves the current [PlayerAnimations] at the call site's position in the hierarchy.
     */
    val playerAnimations: PlayerAnimations
        @Composable
        get() = LocalPlayerAnimations.current
}