package com.theoplayer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.FocusFinder
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.event.track.mediatrack.audio.list.AudioTrackListEventTypes
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.api.event.track.mediatrack.audio.list.AddTrackEvent as AddAudioTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.audio.list.RemoveTrackEvent as RemoveAudioTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.audio.list.TrackListChangeEvent as AudioTrackListChangeEvent
import com.theoplayer.android.api.event.track.texttrack.list.AddTrackEvent as AddTextTrackEvent
import com.theoplayer.android.api.event.track.texttrack.list.RemoveTrackEvent as RemoveTextTrackEvent
import com.theoplayer.android.api.event.track.texttrack.list.TrackListChangeEvent as TextTrackListChangeEvent

/**
 * Wrapper around the audio-track and text-track selection overlay.
 */
open class THEOLanguageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOLanguageView
) : LinearLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null
    private var audioListAdapter: THEOAudioListAdapter? = null
    private var subtitleListAdapter: THEOSubtitleListAdapter? = null

    private val sourceChangeEventListener = EventListener<SourceChangeEvent> {
        audioListAdapter?.clear()
        subtitleListAdapter?.clear()
    }

    private val addAudioTrackEventListener = EventListener<AddAudioTrackEvent> { audioListAdapter?.add(it.track) }
    private val removeAudioTrackEventListener = EventListener<RemoveAudioTrackEvent> { audioListAdapter?.remove(it.track) }
    private val audioTrackListChangeEventListener = EventListener<AudioTrackListChangeEvent> { audioListAdapter?.notifyDataSetChanged() }
    private val addTextTrackEventListener = EventListener<AddTextTrackEvent> { subtitleListAdapter?.add(it.track) }
    private val removeTextTrackEventListener = EventListener<RemoveTextTrackEvent> { subtitleListAdapter?.remove(it.track) }
    private val textTrackListChangeEventListener = EventListener<TextTrackListChangeEvent> { subtitleListAdapter?.notifyDataSetChanged() }

    override fun focusSearch(focused: View?, direction: Int): View? {
        // We should consider ourselves the top of the tree for focus searching;
        // otherwise we could be focus searching in the UIController.
        // Needed for D-pad navigation with a remote control for TVs.
        return FocusFinder.getInstance().findNextFocus(this, focused, direction)
    }

    fun setPlayer(player: Player) {
        this.player = player

        // These would allow THEOLanguageView to consume the focus and the touch events to prevent the elements behind it to be focused/clicked.
        this.isClickable = true
        this.isFocusable = true
        this.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        setupAudioTracks()
        setupTextTracks()
        player.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
    }

    private fun setupAudioTracks() {
        val audioListView = findViewById<THEOTrackListView>(R.id.theo_audio_list)
        audioListView.emptyView = findViewById(R.id.theo_empty_audio_list)

        audioListAdapter = THEOAudioListAdapter(context, R.layout.theo_list_item, R.id.theo_item_value, player!!)
        audioListView.adapter = audioListAdapter

        player?.audioTracks?.addEventListener(AudioTrackListEventTypes.ADDTRACK, addAudioTrackEventListener)
        player?.audioTracks?.addEventListener(AudioTrackListEventTypes.REMOVETRACK, removeAudioTrackEventListener)
        player?.audioTracks?.addEventListener(AudioTrackListEventTypes.TRACKLISTCHANGE, audioTrackListChangeEventListener)

        audioListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            audioListAdapter?.getItem(position)?.isEnabled = true
        }
    }

    private fun setupTextTracks() {
        val subtitleListView = findViewById<THEOTrackListView>(R.id.theo_subtitle_list)
        subtitleListView.emptyView = findViewById(R.id.theo_empty_subtitle_list)

        subtitleListAdapter = THEOSubtitleListAdapter(context, R.layout.theo_list_item, R.id.theo_item_value, player!!)
        subtitleListView.adapter = subtitleListAdapter

        player?.textTracks?.addEventListener(TextTrackListEventTypes.ADDTRACK, addTextTrackEventListener)
        player?.textTracks?.addEventListener(TextTrackListEventTypes.REMOVETRACK, removeTextTrackEventListener)
        player?.textTracks?.addEventListener(TextTrackListEventTypes.TRACKLISTCHANGE, textTrackListChangeEventListener)

        subtitleListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            subtitleListAdapter?.getItem(position)?.mode = TextTrackMode.SHOWING
        }
    }

    fun release() {
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player?.audioTracks?.removeEventListener(AudioTrackListEventTypes.ADDTRACK, addAudioTrackEventListener)
        player?.audioTracks?.removeEventListener(AudioTrackListEventTypes.REMOVETRACK, removeAudioTrackEventListener)
        player?.audioTracks?.removeEventListener(AudioTrackListEventTypes.TRACKLISTCHANGE, audioTrackListChangeEventListener)
        player?.textTracks?.removeEventListener(TextTrackListEventTypes.ADDTRACK, addTextTrackEventListener)
        player?.textTracks?.removeEventListener(TextTrackListEventTypes.REMOVETRACK, removeTextTrackEventListener)
        player?.textTracks?.removeEventListener(TextTrackListEventTypes.TRACKLISTCHANGE, textTrackListChangeEventListener)
        player = null
        audioListAdapter?.clear()
        subtitleListAdapter?.clear()
        audioListAdapter = null
        subtitleListAdapter = null
    }

}