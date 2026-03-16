package com.theoplayer.android.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Enclosed::class)
class CeaUtilTest {

    @RunWith(Parameterized::class)
    class GetLabelForChannelNumberTest(
        private val args: Args,
    ) {

        @Test
        fun `WHEN provided with a channel number THEN returns an expected label`() {
            assertEquals(
                args.expectedLabel,
                getLabelForChannelNumber(args.channelNumber),
            )
        }

        data class Args(
            val channelNumber: Int?,
            val expectedLabel: String?,
        )

        private companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = arrayOf(
                // Boundary checks.
                Args(
                    channelNumber = null,
                    expectedLabel = null,
                ),
                Args(
                    channelNumber = -1,
                    expectedLabel = null,
                ),
                Args(
                    channelNumber = -100,
                    expectedLabel = null,
                ),
                Args(
                    channelNumber = 100,
                    expectedLabel = null,
                ),
                Args(
                    channelNumber = 64,
                    expectedLabel = null,
                ),
                Args(
                    channelNumber = 0,
                    expectedLabel = null,
                ),

                // Regular checks.
                Args(
                    channelNumber = 1,
                    expectedLabel = "CC1",
                ),
                Args(
                    channelNumber = 2,
                    expectedLabel = "CC2",
                ),
                Args(
                    channelNumber = 3,
                    expectedLabel = "CC3",
                ),
                Args(
                    channelNumber = 4,
                    expectedLabel = "CC4",
                ),
                Args(
                    channelNumber = 22,
                    expectedLabel = "CC22",
                ),
                Args(
                    channelNumber = 63,
                    expectedLabel = "CC63",
                ),
            )
        }
    }
}
