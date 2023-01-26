package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ImageButton

/**
 * Button base
 */
@SuppressLint("AppCompatCustomView")
open class THEOImageButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ImageButton(context, attributeSet, defStyleAttr, defStyleRes) {

    private val animationBounceIn = AnimationUtils.loadAnimation(this.context, R.anim.bounce_in)
    private val animationBounceOut = AnimationUtils.loadAnimation(this.context, R.anim.bounce_out)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            this.startAnimation(animationBounceIn)
        } else if (event?.action == MotionEvent.ACTION_UP) {
            this.startAnimation(animationBounceIn)
        }
        return super.onTouchEvent(event)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val shouldHandle =
            keyCode == KeyEvent.KEYCODE_SPACE ||
            keyCode == KeyEvent.KEYCODE_BREAK ||
            keyCode == KeyEvent.KEYCODE_ENTER ||
            keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
            keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE

        if (shouldHandle && event?.action == KeyEvent.ACTION_UP) {
            this.startAnimation(animationBounceOut)
        }
        return super.onKeyDown(keyCode, event)
    }
}