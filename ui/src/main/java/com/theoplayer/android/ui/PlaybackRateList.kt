package com.theoplayer.android.ui

import com.theoplayer.android.ui.common.R as CommonR
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
 * The default playback rates shown in a [PlaybackRateList].
 */
private val defaultPlaybackRates = listOf(0.25, 0.5, 1.0, 1.25, 1.5, 2.0)

/**
 * A list of playback rates, from which the user can choose a desired playback rate.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param playbackRates the list of possible playback rates
 * @param onClick called when a playback rate in the list is clicked
 */
@Composable
fun PlaybackRateList(
    modifier: Modifier = Modifier,
    playbackRates: List<Double> = defaultPlaybackRates,
    onClick: (() -> Unit)? = null
) {
    PlaybackRateList(
        state = rememberPlaybackRateState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        playbackRates = playbackRates,
        onClick = onClick
    )
}

/**
 * A list of playback rates, from which the user can choose a desired playback rate.
 *
 * @param state the [PlaybackRateState] to read and control
 * @param modifier the [Modifier] to be applied to this menu
 * @param playbackRates the list of possible playback rates
 * @param onClick called when a playback rate in the list is clicked
 */
@ExperimentalPlayerStateApi
@Composable
fun PlaybackRateList(
    state: PlaybackRateState,
    modifier: Modifier = Modifier,
    playbackRates: List<Double> = defaultPlaybackRates,
    onClick: (() -> Unit)? = null
) {
    val selectedIndex = playbackRates.indexOf(state.playbackRate).coerceAtLeast(0)
    LazyColumn(
        modifier = modifier.selectableGroup(),
        state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    ) {
        items(
            count = playbackRates.size,
            key = { playbackRates[it] }
        ) {
            val playbackRate = playbackRates[it]
            ListItem(
                headlineContent = { Text(text = formatPlaybackRate(playbackRate)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.playbackRate == playbackRate),
                        onClick = null
                    )
                },
                modifier = Modifier.selectable(
                    selected = state.playbackRate == playbackRate,
                    onClick = {
                        state.select(playbackRate)
                        onClick?.let { it() }
                    },
                    role = Role.RadioButton
                )
            )
        }
    }
}

@Composable
internal fun formatPlaybackRate(rate: Double): String {
    return if (rate == 1.0) {
        stringResource(CommonR.string.theoplayer_ui_playback_rate_normal, rate)
    } else {
        val format = stringResource(CommonR.string.theoplayer_ui_playback_rate_format)
        val decimalFormat = remember(format) { DecimalFormat(format) }
        decimalFormat.format(rate)
    }
}