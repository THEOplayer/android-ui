package com.theoplayer.android.ui.demo

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription

data class Stream(val title: String, val source: SourceDescription)

val streams by lazy {
    listOf(
        Stream(
            title = "Bip Bop (HLS)",
            source = SourceDescription.Builder(
                TypedSource.Builder("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8")
                    .build()
            ).build()
        ),
        Stream(
            title = "Elephant's Dream (HLS)",
            source = SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8")
                    .build()
            ).build()
        ),
        Stream(
            title = "Sintel (DASH) with preroll ad",
            source = SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/dash/webvtt-embedded-in-isobmff/Manifest.mpd")
                    .build()
            ).ads(
                GoogleImaAdDescription.Builder("https://cdn.theoplayer.com/demos/ads/vast/dfp-preroll-no-skip.xml")
                    .timeOffset("start")
                    .build()
            ).build()
        ),
        Stream(
            title = "DASH-IF Livesim",
            source = SourceDescription.Builder(
                TypedSource.Builder("https://livesim.dashif.org/livesim/testpic_2s/Manifest.mpd")
                    .build()
            ).build()
        )
    )
}

object StreamSaver : Saver<Stream, Int> {
    override fun restore(value: Int): Stream? = streams.getOrNull(value)
    override fun SaverScope.save(value: Stream): Int = streams.indexOf(value)
}