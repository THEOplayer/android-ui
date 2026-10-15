package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.VideoQualityState
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.formatBandwidth
import com.theoplayer.android.ui.rememberVideoQualityState

/**
 * A list of video qualities, from which the user can choose a target video quality.
 *
 * @param modifier the [Modifier] to be applied to this list
 * @param onClick called when a video quality in the list is clicked
 */
@ExperimentalTvUiApi
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
 * @param modifier the [Modifier] to be applied to this list
 * @param onClick called when a video quality in the list is clicked
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun QualityList(
    state: VideoQualityState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val selectedIndex = state.targetVideoQuality
        ?.let { state.videoQualities.indexOf(it) + 1 }
        ?: 0
    val selectedRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = modifier.focusRestorer(selectedRequester),
        state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex),
        contentPadding = PaddingValues(vertical = menuItemFocusOverflowPadding),
        verticalArrangement = Arrangement.spacedBy(menuItemVerticalSpacing)
    ) {
        item(key = null) {
            val isActive = state.targetVideoQuality == null
            ListItem(
                selected = isActive,
                onClick = {
                    state.select(null)
                    onClick?.invoke()
                },
                colors = menuItemColors(),
                headlineContent = {
                    Text(text = stringResource(CommonR.string.theoplayer_ui_quality_automatic))
                },
                trailingContent = if (isActive) {
                    { Icon(Icons.Rounded.Check, contentDescription = null) }
                } else {
                    null
                },
                modifier = if (selectedIndex == 0) {
                    Modifier.focusRequester(selectedRequester)
                } else {
                    Modifier
                }
            )
        }
        items(
            count = state.videoQualities.size,
            key = { state.videoQualities[it].uid }
        ) { index ->
            val quality = state.videoQualities[index]
            val isActive = state.targetVideoQuality == quality
            ListItem(
                selected = isActive,
                onClick = {
                    state.select(quality)
                    onClick?.invoke()
                },
                colors = menuItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(
                            CommonR.string.theoplayer_ui_quality_with_height,
                            quality.height
                        )
                    )
                },
                supportingContent = {
                    Text(text = formatBandwidth(quality.bandwidth))
                },
                trailingContent = if (isActive) {
                    { Icon(Icons.Rounded.Check, contentDescription = null) }
                } else {
                    null
                },
                modifier = if (index + 1 == selectedIndex) {
                    Modifier.focusRequester(selectedRequester)
                } else {
                    Modifier
                }
            )
        }
    }
}
