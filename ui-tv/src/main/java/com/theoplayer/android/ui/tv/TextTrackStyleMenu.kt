package com.theoplayer.android.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.TextTrackStyleOption
import com.theoplayer.android.ui.TextTrackStyleState
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.rememberTextTrackStyleState
import com.theoplayer.android.ui.textTrackStyleOptions

/**
 * A menu to change the style of the subtitles, such as the font family, the font color or the
 * background color.
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.TextTrackStyleMenu() {
    TextTrackStyleMenu(state = rememberTextTrackStyleState(LocalTHEOplayerView.current?.player))
}

/**
 * A menu to change the style of the subtitles, such as the font family, the font color or the
 * background color.
 *
 * Remember the state inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param state the [TextTrackStyleState] to read and control
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun MenuScope.TextTrackStyleMenu(state: TextTrackStyleState) {
    Menu(
        title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_text_track_style)) }
    ) {
        val resetRequester = remember { FocusRequester() }
        LazyColumn(
            modifier = Modifier.focusRestorer(resetRequester),
            contentPadding = PaddingValues(vertical = menuItemFocusOverflowPadding),
            verticalArrangement = Arrangement.spacedBy(menuItemVerticalSpacing),
        ) {
            item {
                ListItem(
                    selected = false,
                    onClick = { state.reset() },
                    colors = menuItemColors(),
                    headlineContent = {
                        Text(
                            text =
                                stringResource(CommonR.string.theoplayer_ui_text_track_style_reset)
                        )
                    },
                    modifier = Modifier.focusRequester(resetRequester),
                )
            }
            items(
                items = textTrackStyleOptions.filter { it.isAvailable(state) },
                key = { it.label },
            ) { option ->
                MenuSubmenuItem(
                    onClick = {
                        openMenu {
                            TextTrackStyleOptionMenu(
                                option,
                                rememberTextTrackStyleState(LocalTHEOplayerView.current?.player),
                            )
                        }
                    },
                    headlineContent = { Text(text = stringResource(option.label)) },
                    supportingContent = { Text(text = option.formatCurrentValue(state)) },
                )
            }
        }
    }
}

@ExperimentalPlayerStateApi
@Composable
private fun <T> MenuScope.TextTrackStyleOptionMenu(
    option: TextTrackStyleOption<T>,
    state: TextTrackStyleState,
) {
    Menu(title = { Text(text = stringResource(option.label)) }) {
        val values = option.values
        val selectedIndex = values.indexOfFirst { option.isSelected(state, it) }.coerceAtLeast(0)
        val selectedRequester = remember { FocusRequester() }

        LazyColumn(
            modifier = Modifier.focusRestorer(selectedRequester),
            state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex),
            contentPadding = PaddingValues(vertical = menuItemFocusOverflowPadding),
            verticalArrangement = Arrangement.spacedBy(menuItemVerticalSpacing),
        ) {
            items(count = values.size) { index ->
                val value = values[index]
                val isActive = option.isSelected(state, value)
                ListItem(
                    selected = isActive,
                    onClick = {
                        option.select(state, value)
                        closeCurrentMenu()
                    },
                    colors = menuItemColors(),
                    headlineContent = { Text(text = option.formatValue(value)) },
                    leadingContent = option.colorOf(value)?.let { color -> { ColorDot(color) } },
                    trailingContent =
                        if (isActive) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else {
                            null
                        },
                    modifier =
                        if (index == selectedIndex) {
                            Modifier.focusRequester(selectedRequester)
                        } else {
                            Modifier
                        },
                )
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier =
            Modifier.size(16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    shape = CircleShape,
                )
                .background(color = color.copy(alpha = 1f), shape = CircleShape)
    )
}
