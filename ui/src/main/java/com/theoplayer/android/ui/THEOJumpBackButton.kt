package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.theoplayer.android.api.player.Player
import kotlin.math.max

/**
 * This button will jump back 10 seconds in time in the content.
 *
 * - If you are less then 10 seconds in the video, it will reset back to the start of the content.
 * - This button will not be visible for live content.
 */
@SuppressLint("AppCompatCustomView")
open class THEOJumpBackButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOJumpBackButton
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null

    fun setPlayer(player: Player) {
        this.player = player
    }

    override fun performClick(): Boolean {
        player?.let { player ->
            player.currentTime = max(0.0, player.currentTime - 10)
        }

        return super.performClick()
    }

    fun release() {
        player = null
    }

}