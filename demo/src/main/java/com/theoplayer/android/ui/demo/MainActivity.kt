package com.theoplayer.android.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.ui.DefaultUI

class MainActivity : AppCompatActivity() {
    private lateinit var theoPlayerView: THEOplayerView
    private lateinit var defaultUI: DefaultUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.theoPlayerView = findViewById(R.id.theoplayerview);
        this.defaultUI = findViewById(R.id.theoplayer_default_ui);
        // this.defaultUI.setTHEOplayerView(this.theoPlayerView);
        this.theoPlayerView.settings.isFullScreenOrientationCoupled = true;

        this.theoPlayerView.player.source = SourceDescription.Builder(
            TypedSource.Builder("https://amssamples.streaming.mediaservices.windows.net/7ceb010f-d9a3-467a-915e-5728acced7e3/ElephantsDreamMultiAudio.ism/manifest(format=mpd-time-csf)")
                .build()
        ).build()
    }

    override fun onPause() {
        super.onPause()
        theoPlayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        theoPlayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        theoPlayerView.onDestroy()
    }
}