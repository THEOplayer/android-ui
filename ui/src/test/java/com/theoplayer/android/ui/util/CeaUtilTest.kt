package com.theoplayer.android.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Enclosed::class)
class CeaUtilTest {

    @RunWith(Parameterized::class)
    class IsLabelCeaFormattedTest(
        private val args: Args,
    ) {

        @Test
        fun `WHEN provided with a label THEN returns whether CEA formatted`() {
            assertEquals(
                args.expectedIsCeaFormatted,
                isLabelCeaFormatted(args.label),
            )
        }

        data class Args(
            val label: String?,
            val expectedIsCeaFormatted: Boolean,
        )

        private companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                // False.
                Args(
                    label = null,
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "abc",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "Some label",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "Text with cc1 inlined",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "cC1",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "Cc1",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "CC0",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "CC01",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "CC64",
                    expectedIsCeaFormatted = false,
                ),
                Args(
                    label = "CC128",
                    expectedIsCeaFormatted = false,
                ),

                // True.
                Args(
                    label = "CC1",
                    expectedIsCeaFormatted = true,
                ),
                Args(
                    label = "CC2",
                    expectedIsCeaFormatted = true,
                ),
                Args(
                    label = "CC3",
                    expectedIsCeaFormatted = true,
                ),
                Args(
                    label = "CC4",
                    expectedIsCeaFormatted = true,
                ),
                Args(
                    label = "CC22",
                    expectedIsCeaFormatted = true,
                ),
                Args(
                    label = "CC63",
                    expectedIsCeaFormatted = true,
                ),
            )
        }
    }

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
