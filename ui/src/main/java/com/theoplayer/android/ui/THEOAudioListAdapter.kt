package com.theoplayer.android.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.quality.AudioQuality

/**
 * The THEOAudioListAdapter is responsible for filling the different audio tracks in the language selection view.
 */
open class THEOAudioListAdapter(
    context: Context,
    private val layout_resource: Int,
    private val textViewResourceId: Int,
    private val player: Player
) : ArrayAdapter<MediaTrack<AudioQuality>>(context, layout_resource, textViewResourceId) {

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(textViewResourceId)
        }
    }

    override fun getItem(position: Int): MediaTrack<AudioQuality> = player.audioTracks.getItem(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            row = LayoutInflater.from(context).inflate(layout_resource, parent, false)
            viewHolder = ViewHolder(row)
            row.tag = viewHolder
        } else {
            row = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        val label = getItem(position).label?.takeIf { it.isNotBlank() }
        val language = getItem(position).language?.takeIf { it.isNotBlank() }
        viewHolder.textView.text = label ?: language

        if (getItem(position).isEnabled) {
            viewHolder.textView.setTextColor(ContextCompat.getColor(context, R.color.theoYellow))
        } else {
            viewHolder.textView.setTextColor(ContextCompat.getColor(context, R.color.theoWhite))
        }

        return row
    }
}