package com.theoplayer.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UIControllerDisposeTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var active by mutableIntStateOf(0)

    /**
     * When a [UIController] leaves the composition while the activity lives on, the composition
     * inside its inner [ComposeView] must be disposed as well.
     */
    @Test
    fun controlsAreDisposedWhenUIControllerLeavesComposition() {
        var show by mutableStateOf(true)
        composeRule.setContent {
            if (show) {
                val theoplayerView = remember { THEOplayerView(composeRule.activity) }
                UIController(
                    player = rememberPlayer(theoplayerView),
                    bottomChrome = {
                        DisposableEffect(Unit) {
                            active++
                            onDispose { active-- }
                        }
                    },
                )
            } else {
                Box(Modifier.size(1.dp))
            }
        }
        composeRule.waitUntil(10_000) { active == 1 }

        show = false
        composeRule.waitUntil(10_000) { active == 0 }
    }
}
