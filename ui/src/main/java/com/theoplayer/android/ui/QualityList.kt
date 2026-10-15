package com.theoplayer.android.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import java.text.DecimalFormat

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
    LazyColumn(
        modifier = modifier.selectableGroup(),
        state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    ) {
        item(key = null) {
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.theoplayer_ui_quality_automatic)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.targetVideoQuality == null),
                        onClick = null
                    )
                },
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
                            R.string.theoplayer_ui_quality_with_height,
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

@Composable
internal fun formatBandwidth(bandwidth: Long): String {
    val format = stringResource(
        if (bandwidth >= 1e7) {
            R.string.theoplayer_ui_bandwidth_format_10mbps
        } else if (bandwidth >= 1e6) {
            R.string.theoplayer_ui_bandwidth_format_1mbps
        } else {
            R.string.theoplayer_ui_bandwidth_format_kbps
        }
    )
    val decimalFormat = remember(format) { DecimalFormat(format) }
    return decimalFormat.format(bandwidth / (if (bandwidth >= 1e6) 1e6 else 1e3))
}