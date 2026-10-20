package com.theoplayer.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theoplayer.android.ui.common.R as CommonR

/**
 * A [Menu] to change the style of the subtitles, such as the font family, the font color or the
 * background color.
 */
@Composable
fun MenuScope.TextTrackStyleMenu() {
    TextTrackStyleMenu(state = rememberTextTrackStyleState(LocalTHEOplayerView.current?.player))
}

/**
 * A [Menu] to change the style of the subtitles, such as the font family, the font color or the
 * background color.
 *
 * Remember the state inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param state the [TextTrackStyleState] to read and control
 */
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun MenuScope.TextTrackStyleMenu(state: TextTrackStyleState) {
    Menu(
        title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_caption_style)) },
        actions = {
            TextButton(onClick = { state.reset() }) {
                Text(text = stringResource(CommonR.string.theoplayer_ui_caption_style_reset))
            }
        },
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            textTrackStyleOptions
                .filter { it.isAvailable(state) }
                .forEach { option ->
                    TextTrackStyleItem(
                        labelRes = option.label,
                        value = option.formatCurrentValue(state),
                        onClick = {
                            openMenu {
                                TextTrackStyleOptionMenu(
                                    option,
                                    rememberTextTrackStyleState(
                                        LocalTHEOplayerView.current?.player
                                    ),
                                )
                            }
                        },
                    )
                }
        }
    }
}

@Composable
private fun MenuScope.TextTrackStyleItem(
    @StringRes labelRes: Int,
    value: String,
    onClick: () -> Unit,
) {
    Row {
        Text(
            modifier = Modifier.weight(1f).alignByBaseline(),
            text = stringResource(labelRes),
        )
        TextButton(
            modifier = Modifier.weight(1f).alignByBaseline(),
            onClick = onClick,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = value,
                textAlign = TextAlign.Center,
            )
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
            )
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
        val listState =
            rememberLazyListState(
                initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0),
                initialFirstVisibleItemScrollOffset =
                    if (selectedIndex > 0) {
                        with(LocalDensity.current) { menuListPeekOffset.toPx() }.toInt()
                    } else {
                        0
                    },
            )
        LazyColumn(
            modifier = Modifier.selectableGroup().fadingEdges(listState),
            state = listState,
        ) {
            items(count = values.size) { index ->
                val value = values[index]
                ListItem(
                    headlineContent = { Text(text = option.formatValue(value)) },
                    leadingContent = {
                        RadioButton(
                            selected = option.isSelected(state, value),
                            onClick = null,
                        )
                    },
                    trailingContent = option.colorOf(value)?.let { color -> { ColorDot(color) } },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier =
                        Modifier.selectable(
                            selected = option.isSelected(state, value),
                            onClick = {
                                option.select(state, value)
                                closeCurrentMenu()
                            },
                            role = Role.RadioButton,
                        ),
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
