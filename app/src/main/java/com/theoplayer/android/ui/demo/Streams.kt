package com.theoplayer.android.ui.demo

import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource

data class Stream(val title: String, val source: SourceDescription)

val streams by lazy {
    listOf(
        Stream(
            title = "Elephant's Dream",
            source = SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8")
                    .build()
            ).build()
        )
    )
}