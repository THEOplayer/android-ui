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
        ) {
            override fun toString(): String = version
        }

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

    class CompareVersionTest {
        @Test
        fun `WHEN left major is less than right major THEN left is less than right`() {
            assertEquals(Version.parse("3.0.0") compareTo Version.parse("4.0.0"), -1)
            assertEquals(Version.parse("3.9.1") compareTo Version.parse("4.0.0"), -1)
            assertEquals(Version.parse("10.5.3") compareTo Version.parse("11.0.0"), -1)
        }

        @Test
        fun `WHEN majors are equal and left minor is less than right major THEN left is less than right`() {
            assertEquals(Version.parse("3.0.0") compareTo Version.parse("3.1.0"), -1)
            assertEquals(Version.parse("3.9.1") compareTo Version.parse("3.10.0"), -1)
            assertEquals(Version.parse("10.5.3") compareTo Version.parse("10.6.0"), -1)
        }

        @Test
        fun `WHEN majors and minors are equal and left patch is less than right patch THEN left is less than right`() {
            assertEquals(Version.parse("3.0.0") compareTo Version.parse("3.0.1"), -1)
            assertEquals(Version.parse("3.9.1") compareTo Version.parse("3.9.10"), -1)
            assertEquals(Version.parse("10.5.3") compareTo Version.parse("10.5.4"), -1)
        }

        @Test
        fun `WHEN majors, minors and patches are equal THEN left is equal to right`() {
            assertEquals(Version.parse("3.0.0") compareTo Version.parse("3.0.0"), 0)
            assertEquals(Version.parse("3.9.1") compareTo Version.parse("3.9.1"), 0)
            assertEquals(Version.parse("10.5.3") compareTo Version.parse("10.5.3"), 0)
        }
    }

}
