package com.theoplayer.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.text.DecimalFormat

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
    playbackRates: List<Double> = listOf(0.25, 0.5, 1.0, 1.25, 1.5, 2.0),
    onClick: (() -> Unit)? = null
) {
    val player = Player.current
    val currentPlaybackRate = player?.playbackRate ?: 1
    LazyColumn(modifier = modifier) {
        items(
            count = playbackRates.size,
            key = { playbackRates[it] }
        ) {
            val playbackRate = playbackRates[it]
            ListItem(
                headlineContent = { Text(text = formatPlaybackRate(playbackRate)) },
                leadingContent = {
                    RadioButton(
                        selected = (currentPlaybackRate == playbackRate),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    player?.playbackRate = playbackRate
                    onClick?.let { it() }
                })
            )
        }
    }
}

@Composable
internal fun formatPlaybackRate(rate: Double): String {
    return if (rate == 1.0) {
        stringResource(R.string.theoplayer_ui_playback_rate_normal, rate)
    } else {
        val format = stringResource(R.string.theoplayer_ui_playback_rate_format)
        val decimalFormat = remember(format) { DecimalFormat(format) }
        decimalFormat.format(rate)
    }
}