package com.theoplayer.android.ui.demo.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.FilterChip
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.rememberCarouselState
import com.theoplayer.android.ui.demo.Stream
import com.theoplayer.android.ui.demo.streams
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme

/** The start screen of the Android TV demo: a carousel of streams, and a row to pick the theme. */
@Composable
fun TvHomeScreen(
    modifier: Modifier = Modifier,
    streams: List<Stream>,
    currentTheme: TvPlayerTheme,
    onSelectStream: (Stream) -> Unit,
    onSelectTheme: (TvPlayerTheme) -> Unit,
) {
    val carouselFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { carouselFocusRequester.requestFocus() }

    Column(
        modifier =
            modifier.padding(
                horizontal = safeAreaHorizontalPadding,
                vertical = safeAreaVerticalPadding,
            )
    ) {
        Text(text = "Open Video UI for Android TV", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        StreamCarousel(
            modifier = Modifier.fillMaxWidth().weight(1f).focusRequester(carouselFocusRequester),
            streams = streams,
            onSelectStream = onSelectStream,
        )
        Spacer(Modifier.height(24.dp))
        ThemeRow(currentTheme = currentTheme, onSelectTheme = onSelectTheme)
    }
}

/** A carousel with one slide per stream. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun StreamCarousel(
    modifier: Modifier = Modifier,
    streams: List<Stream>,
    onSelectStream: (Stream) -> Unit,
) {
    val carouselState = rememberCarouselState()
    Carousel(
        itemCount = streams.size,
        modifier = modifier.clip(RoundedCornerShape(16.dp)).keepHorizontalFocusInside(),
        carouselState = carouselState,
        carouselIndicator = {
            CarouselDefaults.IndicatorRow(
                itemCount = streams.size,
                activeItemIndex = carouselState.activeItemIndex,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        },
    ) { index ->
        StreamSlide(
            stream = streams[index],
            onPlay = { onSelectStream(streams[index]) },
        )
    }
}

/**
 * Keeps Left and Right focus moves inside this element, so [Carousel] changes slides instead of
 * moving focus to the theme row.
 *
 * Works around tv-material 1.1.0, where [Carousel]'s own exit handler never cancels the move. See
 * https://issuetracker.google.com/issues/575161906
 */
private fun Modifier.keepHorizontalFocusInside(): Modifier =
    this.focusProperties {
            onExit = {
                if (
                    requestedFocusDirection == FocusDirection.Left ||
                        requestedFocusDirection == FocusDirection.Right
                ) {
                    cancelFocusChange()
                }
            }
        }
        .focusGroup()

/**
 * A single carousel slide, with the stream's background image, title, description and play button.
 */
@Composable
private fun StreamSlide(stream: Stream, onPlay: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(stream.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        SlideScrim()
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(32.dp)) {
            SlideText(stream)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onPlay) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = "Play")
            }
        }
    }
}

/** Dark gradients keep the white slide text readable. */
@Composable
private fun SlideScrim() {
    Box(
        Modifier.fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    0f to Color.Black.copy(alpha = 0.8f),
                    0.6f to Color.Transparent,
                )
            )
            .background(
                Brush.verticalGradient(
                    0.4f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.6f),
                )
            )
    )
}

/** The title and description of a carousel slide. */
@Composable
private fun ColumnScope.SlideText(stream: Stream) {
    Text(
        text = stream.title,
        color = Color.White,
        style = MaterialTheme.typography.displaySmall,
    )
    if (stream.description.isNotEmpty()) {
        Text(
            text = stream.description,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** A row of chips to pick the player theme. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ThemeRow(currentTheme: TvPlayerTheme, onSelectTheme: (TvPlayerTheme) -> Unit) {
    Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        for (theme in TvPlayerTheme.entries) {
            val selected = theme == currentTheme
            FilterChip(
                selected = selected,
                onClick = { onSelectTheme(theme) },
            ) {
                Text(text = theme.title)
            }
        }
    }
}

// Keep content inside the overscan-safe area of TVs.
private val safeAreaHorizontalPadding = 48.dp
private val safeAreaVerticalPadding = 27.dp

@Preview(device = "spec:width=960dp,height=540dp")
@Composable
private fun TvHomeScreenPreview() {
    THEOplayerTvTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TvHomeScreen(
                modifier = Modifier.fillMaxSize(),
                streams = streams,
                currentTheme = TvPlayerTheme.Default,
                onSelectStream = {},
                onSelectTheme = {},
            )
        }
    }
}
