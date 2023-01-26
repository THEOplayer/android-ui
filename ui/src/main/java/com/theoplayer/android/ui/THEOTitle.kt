package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/**
 * Title to be shown for the content.
 *
 * - Will be shown as part of the default state of the player.
 */
@SuppressLint("AppCompatCustomView")
open class THEOTitle @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOTitle
) : TextView(context, attributeSet, defStyleAttr, defStyleRes)
