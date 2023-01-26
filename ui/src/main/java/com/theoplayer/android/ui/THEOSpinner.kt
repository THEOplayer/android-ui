package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar

/**
 * This represents the spinner while the player if buffering.
 *
 */
@SuppressLint("AppCompatCustomView")
open class THEOSpinner @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOSpinner
) : ProgressBar(context, attributeSet, defStyleAttr, defStyleRes)