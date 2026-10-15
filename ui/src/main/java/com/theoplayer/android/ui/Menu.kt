package com.theoplayer.android.ui

import com.theoplayer.android.ui.common.R as CommonR
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * A menu that can be opened on top of the player.
 *
 * The menu has a heading at the top, with a back button and a title text.
 * Clicking on any empty space around the menu's content closes the menu.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param title the title
 * @param backIcon the icon for the back button, typically an [Icon]
 */
@Composable
fun MenuScope.Menu(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    backIcon: @Composable () -> Unit = {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_btn_back)
        )
    },
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier
        .fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { closeCurrentMenu() }
        )
    ) {
        Column(modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* do nothing */ }
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { closeCurrentMenu() }
                ) { backIcon() }
                ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
                    title()
                }
            }
            content()
        }
    }
}
