package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.theoplayer.android.api.player.Player
import kotlin.math.min

/**
 * This button will jump forward 10 seconds in time in the content.
 *
 * - If you are less then 10 seconds from the end the video, it will put you at the exact end.
 * - This button will not be visible for live content.
 */
@SuppressLint("AppCompatCustomView")
open class THEOJumpForwardButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOJumpForwardButton
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null

    fun setPlayer(player: Player) {
        this.player = player
    }

    override fun performClick(): Boolean {
        player?.let { player ->
            if (!player.isEnded) {
                player.currentTime = min(player.duration, player.currentTime + 10)
            }
        }

        return super.performClick()
    }

    fun release() {
        player = null
    }

}