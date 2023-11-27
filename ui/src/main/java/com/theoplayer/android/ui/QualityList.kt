package com.theoplayer.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val player = Player.current
    val videoQualities = player?.videoQualities ?: listOf()
    val targetVideoQuality = player?.targetVideoQuality
    LazyColumn(modifier = modifier) {
        item(key = null) {
            ListItem(
                headlineContent = { Text(text = "Automatic") },
                leadingContent = {
                    RadioButton(
                        selected = (targetVideoQuality == null),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    player?.targetVideoQuality = null
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
                headlineContent = { Text(text = "${quality.height}p") },
                supportingContent = { Text(text = formatBandwidth(quality.bandwidth)) },
                leadingContent = {
                    RadioButton(
                        selected = (targetVideoQuality == quality),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    player?.targetVideoQuality = quality
                    onClick?.let { it() }
                })
            )
        }
    }
}

private val zeroPrecisionFormat = DecimalFormat("#")
private val singlePrecisionFormat = DecimalFormat("#.#")

internal fun formatBandwidth(bandwidth: Long): String {
    return if (bandwidth > 1e7) {
        "${zeroPrecisionFormat.format(bandwidth / 1e6)}Mbps"
    } else if (bandwidth > 1e6) {
        "${singlePrecisionFormat.format(bandwidth / 1e6)}Mbps"
    } else {
        "${zeroPrecisionFormat.format(bandwidth / 1e3)}kbps"
    }
}