package com.theoplayer.android.ui.util

import com.theoplayer.android.api.player.track.Track
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackType
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Enclosed::class)
class TrackExtsTest {

    @RunWith(JUnit4::class)
    class LocalisedLanguageNameTest {

        private val track = mockk<Track>()
        private val locale = mockk<Locale>()

        @Before
        fun setUp() {
            mockkStatic(Locale::class)
        }

        @After
        fun tearDown() {
            clearStaticMockk(Locale::class)
        }

        @Test
        fun `GIVEN language is null THEN localised language is also null`() {
            every { track.language } returns null
            Assert.assertNull(track.localizedLanguageName)
        }

        @Test
        fun `GIVEN language is und THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_UNDEFINED
            Assert.assertNull(track.localizedLanguageName)
        }

        @Test
        fun `GIVEN language is blank THEN localised language is null`() {
            every { track.language } returns TEST_BLANK_STRING
            Assert.assertNull(track.localizedLanguageName)
        }

        @Test
        fun `GIVEN locale returns null as displayLanguage THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.getDisplayName(any()) } returns null

            Assert.assertNull(track.localizedLanguageName)
        }

        @Test
        fun `GIVEN locale returns a blank string as displayLanguage THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.getDisplayName(any()) } returns TEST_BLANK_STRING

            Assert.assertNull(track.localizedLanguageName)
        }

        @Test
        fun `GIVEN locale returns a valid display name THEN returns localised name`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.getDisplayName(any()) } returns LOCALISED_ENGLISH_CODE_NAME

            assertEquals(LOCALISED_ENGLISH_CODE_NAME, track.localizedLanguageName)
        }

        private companion object {
            const val LANGUAGE_CODE_UNDEFINED = "und"
            const val LANGUAGE_CODE_ENGLISH = "en"
            const val LOCALISED_ENGLISH_CODE_NAME = "English"
            const val TEST_BLANK_STRING = "     "
        }
    }

    @RunWith(Parameterized::class)
    class ConstructLabelTest(
        private val args: Args,
    ) {

        private val track = mockk<TextTrack>()

        @Before
        fun setUp() {
            mockkObject(THEOplayerGlobalExt)
            every { THEOplayerGlobalExt.version } returns Version.parse(args.playerVersion)

            every { track.type } returns TextTrackType.CEA608
            every { track.label } returns args.label
            every { track.language } returns args.language
            every { track.captionChannel } returns args.captionChannel

            mockkStatic(Track::localizedLanguageName)
            every { any<Track>().localizedLanguageName } returns args.localizedLanguageName
        }

        @After
        fun tearDown() {
            clearMocks(THEOplayerGlobalExt)
            clearStaticMockk(Track::localizedLanguageName)
        }

        @Test
        fun `WHEN a valid track provided THEN returns a correct label`() {
            assertEquals(
                args.expectedLabel,
                constructLabel(track),
            )
        }

        data class Args(
            val label: String?,
            val language: String?,
            val localizedLanguageName: String?,
            val captionChannel: Int?,
            val playerVersion: String,
            val expectedLabel: String?,
        )

        private companion object {

            const val TEST_PLAYER_VERSION_10 = "10.1.1"
            const val TEST_PLAYER_VERSION_11 = "11.0.10"

            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                // Boundary checks.
                Args(
                    label = null,
                    language = null,
                    localizedLanguageName = null,
                    captionChannel = null,
                    playerVersion = "0.0.0",
                    expectedLabel = null,
                ),

                // v10 checks.
                Args(
                    label = "Hello world",
                    language = null,
                    localizedLanguageName = null,
                    captionChannel = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "Hello world",
                ),
                Args(
                    label = null,
                    language = "en",
                    localizedLanguageName = "English",
                    captionChannel = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "English",
                ),
                Args(
                    label = "en",
                    language = "en",
                    localizedLanguageName = "English",
                    captionChannel = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "English",
                ),
                Args(
                    label = "en",
                    language = null,
                    localizedLanguageName = null,
                    captionChannel = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "en",
                ),
                Args(
                    label = "CC1",
                    language = "en",
                    localizedLanguageName = "English",
                    captionChannel = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "English",
                ),
                Args(
                    label = null,
                    language = null,
                    localizedLanguageName = null,
                    captionChannel = 1,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "CC1",
                ),

                // v11 checks.
                Args(
                    label = "Hello world",
                    language = null,
                    localizedLanguageName = null,
                    captionChannel = null,
                    playerVersion = TEST_PLAYER_VERSION_11,
                    expectedLabel = "Hello world",
                ),
                Args(
                    label = "en",
                    language = "en",
                    localizedLanguageName = "English",
                    captionChannel = null,
                    playerVersion = TEST_PLAYER_VERSION_11,
                    expectedLabel = "en",
                ),
                Args(
                    label = null,
                    language = null,
                    localizedLanguageName = null,
                    captionChannel = 4,
                    playerVersion = TEST_PLAYER_VERSION_11,
                    expectedLabel = "CC4",
                ),
            )
        }
    }
}
