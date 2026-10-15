package com.theoplayer.android.ui.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ProvideTextStyle
import androidx.tv.material3.RadioButton
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.theoplayer.android.ui.MenuContent
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme

/**
 * A side menu displayed at the end edge of the player.
 *
 * @param modifier the [Modifier] to be applied to this menu
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = modifier.fillMaxHeight().width(menuWidth),
            shape = RoundedCornerShape(
                topStart = menuCornerRadius,
                bottomStart = menuCornerRadius
            ),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = menuTitlePadding)
                            .padding(top = menuTitlePadding)
                    ) {
                        title()
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = menuContentPadding)
                        .focusRequester(focusRequester)
                        .focusGroup()
                ) {
                    content()
                }
            }
        }
    }
}

/** A menu row that opens a nested menu. */
@Composable
internal fun MenuSubmenuItem(
    onClick: () -> Unit,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit
) {
    ListItem(
        selected = false,
        onClick = onClick,
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = null) }
    )
}

private val menuWidth = 360.dp
private val menuCornerRadius = 16.dp
private val menuTitlePadding = 24.dp
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
        PreviewMenuScope.Menu(
            title = { Text("Language") }
        ) {
            ListItem(
                selected = true,
                onClick = {},
                headlineContent = { Text("English") },
                leadingContent = { RadioButton(selected = true, onClick = null) }
            )
            ListItem(
                selected = false,
                onClick = {},
                headlineContent = { Text("Subtitles") },
                supportingContent = { Text("Off") },
                trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = null) }
            )
        }
    }
}

private object PreviewMenuScope : MenuScope {
    override fun openMenu(menu: MenuContent) = Unit

    override fun closeCurrentMenu() = Unit
}
