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
                headlineContent = { Text(text = stringResource(R.string.theoplayer_ui_quality_automatic)) },
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