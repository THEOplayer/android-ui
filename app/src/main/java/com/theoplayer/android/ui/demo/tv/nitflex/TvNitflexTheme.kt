package com.theoplayer.android.ui.demo.tv.nitflex

import androidx.compose.runtime.Composable
import androidx.tv.material3.darkColorScheme
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_background
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_error
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_errorContainer
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_inverseOnSurface
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_inversePrimary
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_inverseSurface
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onBackground
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onError
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onErrorContainer
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onPrimary
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onPrimaryContainer
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onSecondary
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onSecondaryContainer
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onSurface
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onSurfaceVariant
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onTertiary
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_onTertiaryContainer
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_outline
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_outlineVariant
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_primary
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_primaryContainer
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_scrim
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_secondary
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_secondaryContainer
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_surface
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_surfaceVariant
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_tertiary
import com.theoplayer.android.ui.demo.nitflex.theme.md_theme_dark_tertiaryContainer
import com.theoplayer.android.ui.tv.ExperimentalTvUiApi
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme

private val NitflexTvColorScheme = darkColorScheme(
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
    scrim = md_theme_dark_scrim
)

/**
 * The Nitflex theme for Android TV, using the same dark colors as the mobile Nitflex theme.
 */
@OptIn(ExperimentalTvUiApi::class)
@Composable
fun TvNitflexTheme(content: @Composable () -> Unit) {
    THEOplayerTvTheme(colorScheme = NitflexTvColorScheme, content = content)
}
