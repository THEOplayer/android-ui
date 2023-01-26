package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

/**
 * This button will allow you to exit the language selection overlay.
 */
@SuppressLint("AppCompatCustomView")
open class THEOLanguageExit @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOLanguageExit
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes)
