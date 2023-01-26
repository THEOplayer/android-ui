package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.RequestCallback
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import kotlinx.coroutines.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * View that represents a thumbnail that hovers above the [THEOSeekbar].
 */
@SuppressLint("AppCompatCustomView")
open class THEOThumbnail @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOThumbnail
) : ImageView(context, attributeSet, defStyleAttr, defStyleRes), SeekBar.OnSeekBarChangeListener {

    private var player: Player? = null
    private var mainHandler: Handler? = null
    private val storedImages: HashMap<String, Bitmap> = HashMap()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val sourceChangeEventListener = EventListener<SourceChangeEvent> { storedImages.clear() }

    fun setPlayer(player: Player) {
        this.player = player
        this.mainHandler = Handler(context.mainLooper)
        this.player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
    }

    /**
     * Only show thumbnails if the player has any.
     * Start download upon touch, clean up data when user lets go of the seekbar.
     */
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val thumbX = seekBar.thumb.bounds.exactCenterX()
        val max = seekBar.width
        val currentTime = seekBar.progress.toDouble()
        val padding = 15f
        val offset = width / 2

        when {
            // left edge
            thumbX < offset -> {
                this.x = padding
            }
            // right edge
            thumbX + padding > max - offset - padding -> {
                this.x = max - width - padding
            }
            // somewhere in middle
            else -> {
                this.x = thumbX - offset + padding
            }
        }

        val track = getActiveThumbnailsTrack()
        if (track == null) {
            reset()
            return
        }

        val baseURI = track.source?.substring(0, track.source?.lastIndexOf('/') ?: 0)
        baseURI ?: return

        track.cues?.filter {
            it.startTime <= currentTime && it.endTime >= currentTime
        }?.forEach { cue ->
            val thumbnailURI = cue.content?.getString("content") ?: return
            handleDownload(baseURI, thumbnailURI)
        }
    }

    // Handle cases different if the thumbnail is offered in a 'tilemap' or single image per timeslot.
    private fun handleDownload(baseURI: String, path: String) {
        val finalPath = if (path.contains("#xywh")) {
            path.split("#xywh=")[0]
        } else {
            path
        }

        launchDownloadImageTask(baseURI, finalPath) { result ->
            var final = result
            if (path.contains("#xywh")) {   // crop if one large tile-map of images.
                storedImages[finalPath] = result
                val coords = path.split("#xywh=")[1].split(",").map { it.toInt() }
                final = Bitmap.createBitmap(result, coords[0], coords[1], coords[2], coords[3])
            } else {
                storedImages[finalPath] = result
            }

            // you can only update UI on main thread.
            mainHandler?.post {
                setImageBitmap(final)
            }
        }
    }

    private fun launchDownloadImageTask(baseURI: String, path: String, callback: RequestCallback<Bitmap>) {
        scope.launch {
            downloadImage(baseURI, path, callback)
        }
    }

    private suspend fun downloadImage(baseURI: String, path: String, callback: RequestCallback<Bitmap>) = coroutineScope {
        val setSourceJob = async {
            val downloadedImage = if (storedImages.containsKey(path)) {
                storedImages[path]
            } else {
                val downloaded = getBitmapFromUri("$baseURI/$path")
                storedImages[path] = downloaded!!
                downloaded
            }
            callback.handleResult(downloadedImage)
        }

        try {
            setSourceJob.await()
        } catch (e: CancellationException) {
            Log.e("Exception", e.toString())
        }
    }

    private fun getBitmapFromUri(path: String): Bitmap? {
        val inputStream: InputStream
        var bitmap: Bitmap? = null
        try {
            val url = URL(path)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"  // optional default is GET
            inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (ex: Exception) {
            Log.e("Exception", ex.toString())
        }
        return bitmap
    }

    private fun getActiveThumbnailsTrack(): TextTrack? {
        player?.textTracks?.forEach { track ->
            if (track.label == "thumbnails" && track.mode == TextTrackMode.SHOWING) {
                return track
            }
        }
        return null
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        if (getActiveThumbnailsTrack() != null) {
            visibility = View.VISIBLE
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        reset()
    }

    private fun reset() {
        storedImages.clear()
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.thumbnail_placeholder))
        visibility = View.GONE
    }

    fun release() {
        reset()
        mainHandler?.removeCallbacksAndMessages(null)
        mainHandler = null
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player = null
    }
}