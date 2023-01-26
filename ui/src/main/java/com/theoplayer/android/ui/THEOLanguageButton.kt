package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

/**
 * This button will show the language overlay, where you can select audio tracks and text tracks.
 */
@SuppressLint("AppCompatCustomView")
open class THEOLanguageButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOLanguageButton
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes)