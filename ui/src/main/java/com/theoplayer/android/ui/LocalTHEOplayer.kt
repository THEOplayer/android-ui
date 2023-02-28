package com.theoplayer.android.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.theoplayer.android.api.player.Player

val LocalTHEOplayer = staticCompositionLocalOf<Player?> { null }