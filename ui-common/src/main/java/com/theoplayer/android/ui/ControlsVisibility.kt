package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/** Visibility state and callbacks for player controls. */
@InternalUiApi
class ControlsVisibility(
    /**
     * Whether the controls are currently visible. This [State] instance is stable across
     * recompositions.
     */
    val visible: State<Boolean>,
    val animationsActive: Boolean,
    val show: () -> Unit,
    val hide: () -> Unit,
)

/** Remembers control visibility and interaction state. */
@InternalUiApi
@Composable
fun rememberControlsVisibility(
    firstPlay: Boolean,
    paused: Boolean,
    forceVisible: Boolean,
    hideDelay: Duration,
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

    val currentFirstPlay by rememberUpdatedState(firstPlay)
    val currentPaused by rememberUpdatedState(paused)
    val currentForceVisible by rememberUpdatedState(forceVisible)
    val visible = remember {
        derivedStateOf {
            when {
                !isReady -> false
                !currentFirstPlay || currentForceVisible -> true
                forceControlsHidden -> false
                else -> recentlyActive || currentPaused
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
        },
    )
}
