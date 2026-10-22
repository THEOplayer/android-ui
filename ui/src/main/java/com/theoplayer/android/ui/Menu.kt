package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theoplayer.android.ui.common.R as CommonR

/**
 * A menu that can be opened on top of the player.
 *
 * The menu has a heading at the top, with a back button and a title text. Inside a [UIController]
 * it is rendered in the shared menu sheet: a bottom sheet on compact players, and a side sheet at
 * the end of the player on wide players (at least 600dp). Clicking outside the sheet closes the
 * menu.
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
            contentDescription = stringResource(CommonR.string.theoplayer_ui_btn_back),
        )
    },
    content: @Composable () -> Unit,
) {
    MenuImpl(
        modifier = modifier,
        title = title,
        backIcon = backIcon,
        actions = null,
        content = content,
    )
}

/**
 * A menu that can be opened on top of the player.
 *
 * The menu has a heading at the top, with a back button and a title text, followed by actions that
 * are aligned to the end of the heading. Inside a [UIController] it is rendered in the shared menu
 * sheet: a bottom sheet on compact players, and a side sheet at the end of the player on wide
 * players (at least 600dp). Clicking outside the sheet closes the menu.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param title the title
 * @param backIcon the icon for the back button, typically an [Icon]
 * @param actions the actions shown at the end of the menu's heading, typically [IconButton]s
 */
@Composable
fun MenuScope.Menu(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    backIcon: @Composable () -> Unit = {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_btn_back),
        )
    },
    actions: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    MenuImpl(
        modifier = modifier,
        title = title,
        backIcon = backIcon,
        actions = actions,
        content = content,
    )
}

@Composable
private fun MenuScope.MenuImpl(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    backIcon: @Composable () -> Unit,
    actions: (@Composable RowScope.() -> Unit)?,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { closeCurrentMenu() }) { backIcon() }
            ProvideTextStyle(value = MaterialTheme.typography.titleMedium) { title() }
            if (actions != null) {
                Spacer(modifier = Modifier.weight(1f))
                actions()
            }
        }
        content()
    }
}
