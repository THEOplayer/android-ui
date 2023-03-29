package com.theoplayer.android.ui.demo.nitflex

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.SkipNext
import androidx.compose.material.icons.sharp.Speed
import androidx.compose.material.icons.sharp.Subtitles
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.*
import com.theoplayer.android.ui.demo.nitflex.theme.NitflexTheme

@Composable
fun NitflexUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null
) {
    ProvideTextStyle(value = TextStyle(color = Color.White)) {
        UIController(
            modifier = modifier,
            config = config,
            source = source,
            centerOverlay = {
                val state = LocalTHEOplayer.current
                if (state?.firstPlay == true) {
                    LoadingSpinner()
                }
            },
            topChrome = {
                val state = LocalTHEOplayer.current
                if (state?.firstPlay == true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        title?.let {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = it,
                                textAlign = TextAlign.Center,
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            centerChrome = {
                val state = LocalTHEOplayer.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (state?.firstPlay == true) {
                        NitflexSeekButton(
                            seekOffset = -10,
                            iconSize = 48.dp,
                            contentPadding = PaddingValues(8.dp)
                        )
                    }
                    NitflexPlayButton(
                        iconModifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(8.dp)
                    )
                    if (state?.firstPlay == true) {
                        NitflexSeekButton(
                            seekOffset = 10,
                            iconSize = 48.dp,
                            contentPadding = PaddingValues(8.dp)
                        )
                    }
                }
            },
            bottomChrome = {
                val state = LocalTHEOplayer.current
                if (state?.firstPlay == true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NitflexSeekBar(modifier = Modifier.weight(1f))
                        CurrentTimeDisplay(showRemaining = true)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            shape = MaterialTheme.shapes.small,
                            onClick = { openMenu { PlaybackRateMenu() } }) {
                            Icon(
                                Icons.Sharp.Speed,
                                tint = Color.White,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Speed")
                        }
                        TextButton(
                            shape = MaterialTheme.shapes.small,
                            onClick = { openMenu { LanguageMenu() } }) {
                            Icon(
                                Icons.Sharp.Subtitles,
                                tint = Color.White,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Audio and subtitles")
                        }
                        TextButton(
                            shape = MaterialTheme.shapes.small,
                            onClick = { /* TODO */ }) {
                            Icon(
                                Icons.Sharp.SkipNext,
                                tint = Color.White,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Next episode")
                        }
                    }
                }
            },
            errorOverlay = {
                val state = LocalTHEOplayer.current
                Box(contentAlignment = Alignment.Center) {
                    ErrorDisplay()
                    // Ensure the user can still exit fullscreen
                    if (state?.fullscreen == true) {
                        FullscreenButton(modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NitflexPreview() {
    NitflexTheme {
        NitflexUI(config = THEOplayerConfig.Builder().build())
    }
}