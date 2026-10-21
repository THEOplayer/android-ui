package com.theoplayer.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(InternalUiApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ControlsVisibilityTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val hideDelay = 2.seconds

    private class Fixture {
        val firstPlay = mutableStateOf(false)
        val paused = mutableStateOf(false)
        val forceVisible = mutableStateOf(false)
        lateinit var controls: ControlsVisibility
    }

    private fun setUpFixture(): Fixture {
        val fixture = Fixture()
        composeRule.setContent {
            fixture.controls =
                rememberControlsVisibility(
                    firstPlay = fixture.firstPlay.value,
                    paused = fixture.paused.value,
                    forceVisible = fixture.forceVisible.value,
                    hideDelay = hideDelay,
                )
        }
        composeRule.waitForIdle()
        return fixture
    }

    private fun advancePastReady() {
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitForIdle()
    }

    @Test
    fun visibleStateIsStableAcrossInputChanges() {
        val fixture = setUpFixture()
        advancePastReady()
        lateinit var initialState: State<Boolean>
        composeRule.runOnIdle { initialState = fixture.controls.visible }

        composeRule.runOnIdle {
            fixture.firstPlay.value = true
            fixture.paused.value = false
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertSame(initialState, fixture.controls.visible)
            // Player is playing with no recent activity: controls hidden.
            assertFalse(initialState.value)
        }
    }

    @Test
    fun hiddenBeforeReadyAndVisibleBeforeFirstPlay() {
        val fixture = setUpFixture()
        composeRule.runOnIdle { assertFalse(fixture.controls.visible.value) }

        advancePastReady()
        composeRule.runOnIdle { assertTrue(fixture.controls.visible.value) }
    }

    @Test
    fun showsTemporarilyWhilePlayingAfterFirstPlay() {
        val fixture = setUpFixture()
        advancePastReady()
        composeRule.runOnIdle { fixture.firstPlay.value = true }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertFalse(fixture.controls.visible.value) }

        composeRule.runOnIdle { fixture.controls.show() }
        composeRule.runOnIdle { assertTrue(fixture.controls.visible.value) }

        composeRule.mainClock.advanceTimeBy(1_900)
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertTrue(fixture.controls.visible.value) }

        composeRule.mainClock.advanceTimeBy(200)
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertFalse(fixture.controls.visible.value) }
    }

    @Test
    fun hideHidesImmediatelyAndShowShowsAgain() {
        val fixture = setUpFixture()
        advancePastReady()
        composeRule.runOnIdle { fixture.firstPlay.value = true }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            fixture.controls.show()
            fixture.controls.hide()
        }
        composeRule.runOnIdle { assertFalse(fixture.controls.visible.value) }

        composeRule.runOnIdle { fixture.controls.show() }
        composeRule.runOnIdle { assertTrue(fixture.controls.visible.value) }
    }

    @Test
    fun pausedAfterFirstPlayShowsControlsUnlessForceHidden() {
        val fixture = setUpFixture()
        advancePastReady()
        composeRule.runOnIdle {
            fixture.firstPlay.value = true
            fixture.paused.value = true
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertTrue(fixture.controls.visible.value) }

        // forceControlsHidden wins over paused.
        composeRule.runOnIdle { fixture.controls.hide() }
        composeRule.runOnIdle { assertFalse(fixture.controls.visible.value) }
    }

    @Test
    fun forceVisibleShowsControlsEvenAfterHideAndHideDelay() {
        val fixture = setUpFixture()
        advancePastReady()
        composeRule.runOnIdle {
            fixture.firstPlay.value = true
            fixture.forceVisible.value = true
            fixture.controls.hide()
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertTrue(fixture.controls.visible.value) }

        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertTrue(fixture.controls.visible.value) }
    }
}
