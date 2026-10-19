package com.theoplayer.android.ui.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalTvUiApi::class)
class SliderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun singlePressChangesValueAndFinishesOnce() {
        val focusRequester = FocusRequester()
        val changes = mutableListOf<Float>()
        val finished = mutableListOf<Unit>()
        setSlider(Modifier.focusRequester(focusRequester)) { modifier ->
            Slider(
                value = 0.2f,
                onValueChange = changes::add,
                modifier = modifier,
                keyStep = 0.1f,
                onValueChangeFinished = { finished += Unit }
            )
        }
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))

        assertEquals(1, changes.size)
        assertEquals(0.3f, changes.single(), 0.0001f)
        assertEquals(1, finished.size)
    }

    @Test
    fun heldKeyAccumulatesRepeatStepsOnSliderState() {
        val focusRequester = FocusRequester()
        val state = SliderState(value = 0.2f)
        val changes = mutableListOf<Float>()
        val draggingDuringChanges = mutableListOf<Boolean>()
        val finished = mutableListOf<Unit>()
        setSlider(Modifier.focusRequester(focusRequester)) { modifier ->
            Slider(
                state = state,
                onValueChange = {
                    changes += it
                    draggingDuringChanges += state.isDragging
                },
                modifier = modifier,
                keyStep = 0.3f,
                keyRepeatStep = 0.1f,
                onValueChangeFinished = { finished += Unit }
            )
        }
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 0))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 1))
        assertTrue(state.isDragging)
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 2))

        assertEquals(3, changes.size)
        assertEquals(0.5f, changes[0], 0.0001f)
        assertEquals(0.6f, changes[1], 0.0001f)
        assertEquals(0.7f, changes[2], 0.0001f)
        assertEquals(listOf(false, true, true), draggingDuringChanges)
        assertTrue(state.isDragging)
        assertEquals(0, finished.size)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))

        assertFalse(state.isDragging)
        assertEquals(1, finished.size)
    }

    @Test
    fun valueClampsAtBothRangeEnds() {
        val focusRequester = FocusRequester()
        val value = mutableFloatStateOf(0f)
        val changes = mutableListOf<Float>()
        setSlider(Modifier.focusRequester(focusRequester)) { modifier ->
            Slider(
                value = value.floatValue,
                onValueChange = changes::add,
                modifier = modifier,
                keyStep = 0.1f
            )
        }
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_DOWN))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_UP))
        composeRule.runOnIdle { value.floatValue = 1f }
        composeRule.waitForIdle()
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))

        assertEquals(2, changes.size)
        assertEquals(0f, changes[0], 0f)
        assertEquals(1f, changes[1], 0f)
    }

    @Test
    fun disabledSliderDoesNotConsumeKeysOrCallCallbacks() {
        val focusRequester = FocusRequester()
        val changes = mutableListOf<Float>()
        val finished = mutableListOf<Unit>()
        setSlider(Modifier.focusRequester(focusRequester)) { modifier ->
            Slider(
                value = 0.5f,
                onValueChange = changes::add,
                modifier = modifier,
                enabled = false,
                onValueChangeFinished = { finished += Unit }
            )
        }
        requestFocus(focusRequester)

        assertFalse(dispatchKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_DOWN))
        assertFalse(dispatchKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_UP))
        assertFalse(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN))
        assertFalse(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))

        assertTrue(changes.isEmpty())
        assertTrue(finished.isEmpty())
    }

    @Test
    fun losingFocusDuringDragFinishesOnce() {
        val focusRequester = FocusRequester()
        val otherFocusRequester = FocusRequester()
        val state = SliderState()
        val finished = mutableListOf<Unit>()
        setSlider(
            modifier = Modifier.focusRequester(focusRequester),
            otherFocusRequester = otherFocusRequester
        ) { modifier ->
            Slider(
                state = state,
                onValueChange = {},
                modifier = modifier,
                onValueChangeFinished = { finished += Unit }
            )
        }
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 0))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 1))
        assertTrue(state.isDragging)
        composeRule.runOnIdle { otherFocusRequester.requestFocus() }
        composeRule.waitForIdle()

        assertFalse(state.isDragging)
        assertEquals(1, finished.size)
    }

    @Test
    fun disablingDuringDragFinishesOnce() {
        val focusRequester = FocusRequester()
        val enabled = mutableStateOf(true)
        val state = SliderState()
        val finished = mutableListOf<Unit>()
        setSlider(Modifier.focusRequester(focusRequester)) { modifier ->
            Slider(
                state = state,
                onValueChange = {},
                modifier = modifier,
                enabled = enabled.value,
                onValueChangeFinished = { finished += Unit }
            )
        }
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 0))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 1))
        assertTrue(state.isDragging)
        composeRule.runOnIdle { enabled.value = false }
        composeRule.waitForIdle()

        assertFalse(state.isDragging)
        assertEquals(1, finished.size)
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))
        assertEquals(1, finished.size)
    }

    @Test
    fun valueOverloadFollowsExternalValueWhenNotDragging() {
        val value = mutableFloatStateOf(0.2f)
        setSlider { modifier ->
            Slider(
                value = value.floatValue,
                onValueChange = {},
                modifier = modifier,
                thumb = { state ->
                    Box(
                        Modifier
                            .size(1.dp)
                            .semantics { contentDescription = "slider-value-${state.value}" }
                    )
                }
            )
        }

        composeRule.onNodeWithContentDescription("slider-value-0.2").assertExists()
        composeRule.runOnIdle { value.floatValue = 0.75f }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("slider-value-0.75").assertExists()
    }

    @Test
    fun sliderIsFocusable() {
        val focusRequester = FocusRequester()
        setSlider(Modifier.focusRequester(focusRequester).testTag("slider")) { modifier ->
            Slider(value = 0.5f, onValueChange = {}, modifier = modifier)
        }

        requestFocus(focusRequester)

        composeRule.onNodeWithTag("slider").assertIsFocused()
    }

    private fun setSlider(
        modifier: Modifier = Modifier,
        otherFocusRequester: FocusRequester? = null,
        content: @Composable (Modifier) -> Unit
    ) {
        composeRule.setContent {
            THEOplayerTvTheme {
                Column {
                    content(modifier)
                    if (otherFocusRequester != null) {
                        Box(
                            Modifier
                                .size(16.dp)
                                .focusRequester(otherFocusRequester)
                                .focusable()
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun requestFocus(focusRequester: FocusRequester) {
        composeRule.runOnIdle { focusRequester.requestFocus() }
    }

    private fun dispatchKey(
        keyCode: Int,
        action: Int,
        repeat: Int = 0
    ): Boolean = dispatchKeyEvent(
        composeRule.activity,
        KeyEvent(1_000L, eventTime++, action, keyCode, repeat)
    )

    private var eventTime = 1_001L
}
