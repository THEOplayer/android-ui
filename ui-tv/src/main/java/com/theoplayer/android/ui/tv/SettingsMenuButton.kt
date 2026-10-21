package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.common.R as CommonR

/**
 * A button that opens the [settings menu][SettingsMenu].
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and content
 * @param content the button content
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.SettingsMenuButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Settings,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_menu_settings),
        )
    },
) {
    IconButton(
        modifier = modifier,
        onClick = { openMenu { SettingsMenu() } },
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}
