package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Visibility state and callbacks for player controls.
 */
@InternalUiApi
class ControlsVisibility(
    val visible: State<Boolean>,
    val animationsActive: Boolean,
    val show: () -> Unit,
    val hide: () -> Unit
)

/**
 * Remembers control visibility and interaction state.
 */
@InternalUiApi
@Composable
fun rememberControlsVisibility(
    firstPlay: Boolean,
    paused: Boolean,
    forceVisible: Boolean,
    hideDelay: Duration
): ControlsVisibility {
    var activityCount by remember { mutableIntStateOf(0) }
    var recentlyActive by remember { mutableStateOf(false) }
    LaunchedEffect(activityCount, hideDelay) {
        if (activityCount > 0) {
            recentlyActive = true
            delay(hideDelay)
            recentlyActive = false
        }
    }

    var forceControlsHidden by remember { mutableStateOf(false) }
    val inspectionMode = LocalInspectionMode.current
    var isReady by remember { mutableStateOf(inspectionMode) }
    LaunchedEffect(Unit) {
        delay(50.milliseconds)
        isReady = true
    }

    val visible = remember(firstPlay, paused, forceVisible) {
        derivedStateOf {
            when {
                !isReady -> false
                !firstPlay || forceVisible -> true
                forceControlsHidden -> false
                else -> recentlyActive || paused
            }
        }
    }
    return ControlsVisibility(
        visible = visible,
        animationsActive = isReady,
        show = {
            forceControlsHidden = false
            activityCount++
        },
        hide = {
            forceControlsHidden = true
            activityCount++
        }
    )
}
