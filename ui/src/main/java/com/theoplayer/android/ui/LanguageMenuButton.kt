package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * A button that opens the [language menu][LanguageMenu].
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 */
@Composable
fun MenuScope.LanguageMenuButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.language),
            contentDescription = stringResource(R.string.theoplayer_ui_menu_language)
        )
    }
) {
    val player = Player.current
    if (!showLanguageMenuButton(player)) {
        // Hide when no alternative audio or subtitle tracks are available
        return
    }

    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = { openMenu { LanguageMenu() } }) {
        content()
    }
}

internal fun showLanguageMenuButton(player: Player?): Boolean {
    return showAudioTracks(player) || showSubtitleTracks(player)
}
