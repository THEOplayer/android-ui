package com.theoplayer.android.ui.util

import com.theoplayer.android.api.THEOplayerGlobal
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(Enclosed::class)
class VersionUtilTest {

    @RunWith(JUnit4::class)
    class RunForPlayerWithTest {

        private val actionAbove = mockk<() -> Unit>()
        private val actionBelow = mockk<() -> Unit>()

        @Before
        fun setUp() {
            mockkStatic(THEOplayerGlobal::class)

            every { actionAbove.invoke() } returns Unit
            every { actionBelow.invoke() } returns Unit
        }

        @Test
        fun `WHEN THEOplayerGlobal version is null THEN executes action for version below`() {
            every { THEOplayerGlobal.getVersion() } returns null

            runForPlayerWith(
                desiredMajorVersion = 2,
                actionIfEqualOrAbove = actionAbove,
                actionIfBelow = actionBelow,
            )

            verify { actionBelow() }
        }

        @Test
        fun `WHEN THEOplayerGlobal version is invalid THEN executes action for version below`() {
            every { THEOplayerGlobal.getVersion() } returns TEST_PLAYER_VERSION_INVALID

            runForPlayerWith(
                desiredMajorVersion = 2,
                actionIfEqualOrAbove = actionAbove,
                actionIfBelow = actionBelow,
            )

            verify { actionBelow() }
        }

        @Test
        fun `WHEN THEOplayerGlobal version is valid and old THEN executes action for version below`() {
            every { THEOplayerGlobal.getVersion() } returns TEST_PLAYER_VERSION_OLD

            runForPlayerWith(
                desiredMajorVersion = 2,
                actionIfEqualOrAbove = actionAbove,
                actionIfBelow = actionBelow,
            )

            verify { actionBelow() }
        }

        @Test
        fun `WHEN THEOplayerGlobal version is valid and new THEN executes action for version above`() {
            every { THEOplayerGlobal.getVersion() } returns TEST_PLAYER_VERSION_NEW

            runForPlayerWith(
                desiredMajorVersion = 2,
                actionIfEqualOrAbove = actionAbove,
                actionIfBelow = actionBelow,
            )

            verify { actionAbove() }
        }

        private companion object {
            const val TEST_PLAYER_VERSION_INVALID = "invalid version"
            const val TEST_PLAYER_VERSION_NEW = "2.3.1"
            const val TEST_PLAYER_VERSION_OLD = "1.1.5"
        }
    }

}
