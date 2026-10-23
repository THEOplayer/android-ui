package com.theoplayer.android.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.theoplayer.android.api.THEOplayerView

/** The [THEOplayerView] controlled by the enclosing [UIController]. */
val LocalTHEOplayerView = staticCompositionLocalOf<THEOplayerView?> { null }
