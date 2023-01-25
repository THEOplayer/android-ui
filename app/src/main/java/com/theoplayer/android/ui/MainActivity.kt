package com.theoplayer.android.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource

class MainActivity : AppCompatActivity() {

    private lateinit var theoplayerView: THEOplayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupTHEOplayer()
        setSource()
        theoplayerView.player.play()
    }

    private fun setupTHEOplayer() {
        val theoplayerConfig = THEOplayerConfig.Builder()
            .build()
        theoplayerView = THEOplayerView(this, theoplayerConfig)
        theoplayerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val tpvContainer = findViewById<FrameLayout>(R.id.tpv_container)
        tpvContainer.addView(theoplayerView)
    }

    private fun setSource() {
        theoplayerView.player.source = SourceDescription.Builder(
            TypedSource.Builder("https://cdn.theoplayer.com/video/big_buck_bunny/big_buck_bunny.m3u8")
                .build()
        )
            .build()
    }

    override fun onPause() {
        super.onPause()
        theoplayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        theoplayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        theoplayerView.onDestroy()
    }

}