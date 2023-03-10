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
fun QualityList(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val state = LocalTHEOplayer.current
    val videoQualities = state?.videoQualities ?: listOf()
    val targetVideoQuality = state?.targetVideoQuality
    LazyColumn(modifier = modifier) {
        item(key = null) {
            ListItem(
                headlineText = { Text(text = "Automatic") },
                leadingContent = {
                    RadioButton(
                        selected = (targetVideoQuality == null),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    state?.targetVideoQuality = null
                    onClick?.let { it() }
                })
            )
        }
        items(
            count = videoQualities.size,
            key = { videoQualities[it].uid }
        ) {
            val quality = videoQualities[it]
            ListItem(
                headlineText = { Text(text = "${quality.height}p") },
                supportingText = { Text(text = formatBandwidth(quality.bandwidth)) },
                leadingContent = {
                    RadioButton(
                        selected = (targetVideoQuality == quality),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    state?.targetVideoQuality = quality
                    onClick?.let { it() }
                })
            )
        }
    }
}

internal val zeroPrecisionFormat = DecimalFormat("#")
internal val singlePrecisionFormat = DecimalFormat("#.#")

internal fun formatBandwidth(bandwidth: Long): String {
    return if (bandwidth > 1e7) {
        "${zeroPrecisionFormat.format(bandwidth / 1e6)}Mbps"
    } else if (bandwidth > 1e6) {
        "${singlePrecisionFormat.format(bandwidth / 1e6)}Mbps"
    } else {
        "${zeroPrecisionFormat.format(bandwidth / 1e3)}kbps"
    }
}