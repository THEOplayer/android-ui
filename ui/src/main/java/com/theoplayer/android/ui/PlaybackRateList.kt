package com.theoplayer.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackRateList(
    playbackRates: List<Double> = listOf(0.25, 0.5, 1.0, 1.25, 1.5, 2.0),
    onClick: (() -> Unit)? = null
) {
    val state = LocalTHEOplayer.current
    val currentPlaybackRate = state?.playbackRate ?: 1
    LazyColumn {
        items(
            count = playbackRates.size,
            key = { playbackRates[it] }
        ) {
            val playbackRate = playbackRates[it]
            ListItem(
                headlineText = { Text(text = formatPlaybackRate(playbackRate)) },
                leadingContent = {
                    RadioButton(
                        selected = (currentPlaybackRate == playbackRate),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    state?.playbackRate = playbackRate
                    onClick?.let { it() }
                })
            )
        }
    }
}

internal val playbackRateFormat = DecimalFormat("#.##")

fun formatPlaybackRate(rate: Double): String {
    return if (rate == 1.0) {
        "Normal"
    } else {
        "${playbackRateFormat.format(rate)}x"
    }
}