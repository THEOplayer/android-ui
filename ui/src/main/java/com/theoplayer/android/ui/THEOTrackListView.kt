package com.theoplayer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView

/**
 * Represents the list view of tracks inside the language selection overlay.
 */
open class THEOTrackListView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOTrackListView
) : ListView(context, attributeSet, defStyleAttr, defStyleRes)