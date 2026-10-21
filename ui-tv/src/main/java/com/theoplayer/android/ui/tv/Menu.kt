package com.theoplayer.android.ui.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ProvideTextStyle
import androidx.tv.material3.Text
import com.theoplayer.android.ui.MenuContent
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme

/**
 * A side menu displayed at the end edge of the player.
 *
 * Inside a [UIController] the menu is rendered in the shared menu sheet at the end
 * edge, so opening a submenu only animates the menu contents inside it.
 *
 * @param modifier the [Modifier] to be applied to this menu's contents
 * @param title the title
 * @param content the menu content
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.Menu(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
            Box(
                modifier = Modifier
                    .padding(
                        start = menuTitlePadding,
                        top = menuTitlePadding,
                        end = menuTitlePadding,
                        bottom = menuTitleBottomSpacing
                    )
            ) {
                title()
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = menuContentPadding)
                .focusRequester(focusRequester)
                .focusGroup(),
            verticalArrangement = Arrangement.spacedBy(menuItemVerticalSpacing)
        ) {
            content()
        }
    }
}

/** A menu row that opens a nested menu. */
@Composable
internal fun MenuSubmenuItem(
    onClick: () -> Unit,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit,
    leadingContent: @Composable BoxScope.() -> Unit
) {
    ListItem(
        selected = false,
        onClick = onClick,
        colors = menuItemColors(),
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = null) }
    )
}

@Composable
internal fun menuItemColors() = ListItemDefaults.colors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant
)

internal val menuItemVerticalSpacing = 8.dp

// Leaves room for the focused item's scale so lazy lists don't clip it.
internal val menuItemFocusOverflowPadding = 4.dp

private val menuTitlePadding = 24.dp
private val menuTitleBottomSpacing = 16.dp
private val menuContentPadding = 12.dp

@OptIn(ExperimentalTvUiApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    device = "spec:width=960dp,height=540dp"
)
@Composable
private fun MenuPreview() {
    THEOplayerTvTheme {
        MenuSheet {
            PreviewMenuScope.Menu(
                title = { Text("Language") }
            ) {
                ListItem(
                    selected = true,
                    onClick = {},
                    colors = menuItemColors(),
                    headlineContent = { Text("English") },
                    trailingContent = { Icon(Icons.Rounded.Check, contentDescription = null) }
                )
                ListItem(
                    selected = false,
                    onClick = {},
                    colors = menuItemColors(),
                    headlineContent = { Text("Subtitles") },
                    supportingContent = { Text("Off") },
                    leadingContent = {
                        Icon(Icons.Rounded.ClosedCaption, contentDescription = null)
                    },
                    trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = null) }
                )
            }
        }
    }
}

private object PreviewMenuScope : MenuScope {
    override fun openMenu(menu: MenuContent) = Unit

    override fun closeCurrentMenu() = Unit
}
