package com.theoplayer.android.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.player.Player as NativePlayer
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1000dp-h1000dp")
class ConstrainedAspectRatioTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var size = IntSize.Zero

    private fun assertSize(expectedWidthDp: Int, expectedHeightDp: Int) {
        with(composeRule.density) {
            assertEquals(expectedWidthDp.toFloat(), size.width.toDp().value, 0.5f)
            assertEquals(expectedHeightDp.toFloat(), size.height.toDp().value, 0.5f)
        }
    }

    @Test
    fun derivesHeightFromWidth() {
        composeRule.setContent {
            Box(Modifier.size(1000.dp), propagateMinConstraints = false) {
                Box(
                    Modifier
                        .onSizeChanged { size = it }
                        .width(320.dp)
                        .constrainedAspectRatio(16f / 9f)
                )
            }
        }
        composeRule.waitForIdle()
        assertSize(320, 180)
    }

    @Test
    fun derivesHeightFromWidthForSquareRatio() {
        composeRule.setContent {
            Box(Modifier.size(1000.dp), propagateMinConstraints = false) {
                Box(
                    Modifier
                        .onSizeChanged { size = it }
                        .width(320.dp)
                        .constrainedAspectRatio(1f)
                )
            }
        }
        composeRule.waitForIdle()
        assertSize(320, 320)
    }

    @Test
    fun derivesHeightFromFillMaxWidth() {
        composeRule.setContent {
            Box(Modifier.width(320.dp).height(1000.dp)) {
                Box(
                    Modifier
                        .onSizeChanged { size = it }
                        .fillMaxWidth()
                        .constrainedAspectRatio(16f / 9f)
                )
            }
        }
        composeRule.waitForIdle()
        assertSize(320, 180)
    }

    @Test
    fun derivesWidthFromHeight() {
        composeRule.setContent {
            Box(Modifier.size(1000.dp), propagateMinConstraints = false) {
                Box(
                    Modifier
                        .onSizeChanged { size = it }
                        .height(180.dp)
                        .constrainedAspectRatio(16f / 9f)
                )
            }
        }
        composeRule.waitForIdle()
        assertSize(320, 180)
    }

    @Test
    fun neverViolatesIncomingConstraints() {
        composeRule.setContent {
            Box(Modifier.size(1000.dp), propagateMinConstraints = false) {
                Box(
                    Modifier
                        .onSizeChanged { size = it }
                        .size(320.dp, 100.dp)
                        .constrainedAspectRatio(16f / 9f)
                )
            }
        }
        composeRule.waitForIdle()
        assertSize(320, 100)
    }

    @Test
    fun playerAspectRatioFallsBackToSixteenByNine() {
        val theoplayerView = PlayerBackedTHEOplayerView(composeRule.activity, mockk(relaxed = true))
        composeRule.setContent {
            Box(Modifier.width(320.dp).height(1000.dp)) {
                UIController(
                    modifier = Modifier.onSizeChanged { size = it },
                    theoplayerView = theoplayerView
                )
            }
        }
        composeRule.waitForIdle()
        assertSize(320, 180)
    }

    private class PlayerBackedTHEOplayerView(
        context: Context,
        private val nativePlayer: NativePlayer
    ) : THEOplayerView(context) {
        override fun getPlayer(): NativePlayer = nativePlayer
    }
}
