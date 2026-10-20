package com.theoplayer.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ToggleControlsOnTapTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class Fixture {
        val controlsVisible = mutableStateOf<State<Boolean>>(mutableStateOf(true))
        val showControlsTemporarily = mutableStateOf<() -> Unit>({})
        val hideControls = mutableStateOf<() -> Unit>({})
    }

    private fun setUpFixture(): Fixture {
        val fixture = Fixture()
        composeRule.setContent {
            Box(
                Modifier
                    .size(300.dp)
                    .testTag("target")
                    .toggleControlsOnTap(
                        controlsVisible = fixture.controlsVisible.value,
                        showControlsTemporarily = fixture.showControlsTemporarily.value,
                        hideControls = fixture.hideControls.value
                    )
            )
        }
        composeRule.waitForIdle()
        return fixture
    }

    private fun tap() {
        composeRule.onNodeWithTag("target").performTouchInput { click() }
        composeRule.waitForIdle()
    }

    @Test
    fun usesLatestControlsVisibleState() {
        val fixture = setUpFixture()
        var showCount = 0
        var hideCount = 0
        composeRule.runOnIdle {
            fixture.showControlsTemporarily.value = { showCount++ }
            fixture.hideControls.value = { hideCount++ }
        }
        composeRule.waitForIdle()

        tap()
        composeRule.runOnIdle {
            assertEquals(1, hideCount)
        }

        // Swap the State instance; the running gesture handler must observe it.
        composeRule.runOnIdle {
            fixture.controlsVisible.value = mutableStateOf(false)
            showCount = 0
            hideCount = 0
        }
        composeRule.waitForIdle()

        tap()
        composeRule.runOnIdle {
            assertEquals(0, hideCount)
            assertTrue(showCount >= 1)
        }
    }

    @Test
    fun usesLatestCallbacks() {
        val fixture = setUpFixture()
        composeRule.runOnIdle {
            fixture.controlsVisible.value = mutableStateOf(false)
        }
        composeRule.waitForIdle()

        var showCount1 = 0
        var hideCount1 = 0
        composeRule.runOnIdle {
            fixture.showControlsTemporarily.value = { showCount1++ }
            fixture.hideControls.value = { hideCount1++ }
        }
        composeRule.waitForIdle()

        tap()
        composeRule.runOnIdle {
            assertEquals(2, showCount1)
        }

        val showCount1BeforeSwap = showCount1
        val hideCount1BeforeSwap = hideCount1
        var showCount2 = 0
        var hideCount2 = 0
        composeRule.runOnIdle {
            fixture.showControlsTemporarily.value = { showCount2++ }
            fixture.hideControls.value = { hideCount2++ }
        }
        composeRule.waitForIdle()

        tap()
        composeRule.runOnIdle {
            assertTrue(showCount2 >= 1)
            assertEquals(0, hideCount2)
            assertEquals(showCount1BeforeSwap, showCount1)
            assertEquals(hideCount1BeforeSwap, hideCount1)
        }
    }

    @Test
    fun tapShowsWhenHiddenAndHidesWhenVisible() {
        val fixture = setUpFixture()
        var showCount = 0
        var hideCount = 0
        val visible = mutableStateOf(false)
        composeRule.runOnIdle {
            fixture.controlsVisible.value = visible
            fixture.showControlsTemporarily.value = { showCount++ }
            fixture.hideControls.value = { hideCount++ }
        }
        composeRule.waitForIdle()

        // Hidden controls: press and release each show the controls.
        tap()
        composeRule.runOnIdle {
            assertEquals(2, showCount)
            assertEquals(0, hideCount)
        }

        // Visible controls: press shows, release hides once and does not show again.
        composeRule.runOnIdle {
            visible.value = true
            showCount = 0
            hideCount = 0
        }
        composeRule.waitForIdle()

        tap()
        composeRule.runOnIdle {
            assertEquals(1, hideCount)
            assertEquals(1, showCount)
        }
    }
}
