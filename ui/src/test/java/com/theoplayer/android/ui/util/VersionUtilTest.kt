package com.theoplayer.android.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Enclosed::class)
internal class VersionUtilTest {

    @RunWith(Parameterized::class)
    class ParseVersionTest(
        private val args: Args,
    ) {

        @Test
        fun `WHEN a version string provided THEN returns a correct major version`() {
            assertEquals(
                args.expected,
                Version.parse(args.version),
            )
        }

        data class Args(
            val version: String,
            val expected: Version,
        )

        private companion object {

            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                Args(
                    version = "11.0.0",
                    expected = Version(major = 11, minor = 0, patchAndPrerelease = "0"),
                ),
                Args(
                    version = "1.2.3",
                    expected = Version(major = 1, minor = 2, patchAndPrerelease = "3"),
                ),
                Args(
                    version = "9.8.7",
                    expected = Version(major = 9, minor = 8, patchAndPrerelease = "7"),
                ),
                Args(
                    version = "1.1.0-beta01",
                    expected = Version(major = 1, minor = 1, patchAndPrerelease = "0-beta01"),
                ),
                Args(
                    version = "2.1.0-beta.1.0",
                    expected = Version(major = 2, minor = 1, patchAndPrerelease = "0-beta.1.0"),
                ),
                Args(
                    version = "16.8.2+01",
                    expected = Version(major = 16, minor = 8, patchAndPrerelease = "2+01"),
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
