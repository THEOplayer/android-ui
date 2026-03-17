package com.theoplayer.android.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Enclosed::class)
class VersionUtilTest {

    @RunWith(Parameterized::class)
    class ParseVersionTest(
        private val args: Args,
    ) {

        @Test
        fun `WHEN a version string provided THEN returns a correct major version`() {
            assertEquals(
                args.expectedMajorVersion,
                Version.parse(args.version).major,
            )
        }

        data class Args(
            val version: String,
            val expectedMajorVersion: Int,
        )

        private companion object {

            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                Args(
                    version = "11.0.0",
                    expectedMajorVersion = 11,
                ),
                Args(
                    version = "1.2.3",
                    expectedMajorVersion = 1,
                ),
                Args(
                    version = "9.8.7",
                    expectedMajorVersion = 9,
                ),
                Args(
                    version = "1.1.0-beta01",
                    expectedMajorVersion = 1,
                ),
                Args(
                    version = "2.1.0-beta.1.0",
                    expectedMajorVersion = 2,
                ),
                Args(
                    version = "16.8.2+01",
                    expectedMajorVersion = 16,
                ),
            )
        }
    }

    @RunWith(Parameterized::class)
    class InvalidVersionTest(
        private val version: String
    ) {

        @Test
        fun `WHEN an invalid version string provided THEN throws an error`() {
            assertThrows(IllegalArgumentException::class.java) {
                Version.parse(version)
            }
        }

        private companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                "",
                "not a version string",
                "1.00"
            )
        }
    }

}
