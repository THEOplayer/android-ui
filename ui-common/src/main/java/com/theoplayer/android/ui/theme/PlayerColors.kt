package com.theoplayer.android.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.theoplayer.android.ui.InternalUiApi

/** Additional colors used by THEOplayer UI components. */
@Immutable
data class PlayerColors(
    /** The color of the circle icon of a live button when playing at the live point. */
    val liveButtonLive: Color = Color.Red,
    /** The color of the circle icon of a live button when playing behind the live point. */
    val liveButtonDvr: Color = Color.Gray,
)

/** Player component colors shared by UI modules. */
@InternalUiApi val LocalPlayerColors = staticCompositionLocalOf { PlayerColors() }
