package com.theoplayer.android.ui

import com.theoplayer.android.ui.common.R as CommonR
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role

/**
 * A list of video qualities, from which the user can choose a target video quality.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when a video quality in the list is clicked
 */
@Composable
fun QualityList(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    QualityList(
        state = rememberVideoQualityState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        onClick = onClick
    )
}

/**
 * A list of video qualities, from which the user can choose a target video quality.
 *
 * @param state the [VideoQualityState] to read and control
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when a video quality in the list is clicked
 */
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun QualityList(
    state: VideoQualityState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val selectedIndex = state.targetVideoQuality
        ?.let { state.videoQualities.indexOf(it) + 1 }
        ?.coerceAtLeast(0)
        ?: 0
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0),
        initialFirstVisibleItemScrollOffset = if (selectedIndex > 0) {
            with(LocalDensity.current) { menuListPeekOffset.toPx() }.toInt()
        } else {
            0
        }
    )
    LazyColumn(
        modifier = modifier.selectableGroup().fadingEdges(listState),
        state = listState
    ) {
        item(key = null) {
            ListItem(
                headlineContent = { Text(text = stringResource(CommonR.string.theoplayer_ui_quality_automatic)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.targetVideoQuality == null),
                        onClick = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.selectable(
                    selected = state.targetVideoQuality == null,
                    onClick = {
                        state.select(null)
                        onClick?.let { it() }
                    },
                    role = Role.RadioButton
                )
            )
        }
        items(
            count = state.videoQualities.size,
            key = { state.videoQualities[it].uid }
        ) {
            val quality = state.videoQualities[it]
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(
                            CommonR.string.theoplayer_ui_quality_with_height,
                            quality.height
                        )
                    )
                },
                supportingContent = { Text(text = formatBandwidth(quality.bandwidth)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.targetVideoQuality == quality),
                        onClick = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.selectable(
                    selected = state.targetVideoQuality == quality,
                    onClick = {
                        state.select(quality)
                        onClick?.let { it() }
                    },
                    role = Role.RadioButton
                )
            )
        }
    }
}