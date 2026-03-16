package com.theoplayer.android.ui.util

import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.track.TrackEvent
import com.theoplayer.android.api.player.track.Track
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.api.player.track.texttrack.TextTrackReadyState
import com.theoplayer.android.api.player.track.texttrack.TextTrackType
import com.theoplayer.android.api.player.track.texttrack.cue.TextTrackCueList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
    class LocalisedLanguageTest {

        private val track = mockk<Track>()
        private val locale = mockk<Locale>()

        @Before
        fun setUp() {
            mockkStatic(Locale::class)
        }

        @Test
        fun `GIVEN language is null THEN localised language is also null`() {
            every { track.language } returns null
            Assert.assertNull(track.localizedLanguage)
        }

        @Test
        fun `GIVEN language is und THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_UNDEFINED
            Assert.assertNull(track.localizedLanguage)
        }

        @Test
        fun `GIVEN language is blank THEN localised language is null`() {
            every { track.language } returns TEST_BLANK_STRING
            Assert.assertNull(track.localizedLanguage)
        }

        @Test
        fun `GIVEN locale returns null as displayLanguage THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.displayLanguage } returns null

            Assert.assertNull(track.localizedLanguage)
        }

        @Test
        fun `GIVEN locale returns a blank string as displayLanguage THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.displayLanguage } returns TEST_BLANK_STRING

            Assert.assertNull(track.localizedLanguage)
        }

        @Test
        fun `GIVEN locale returns a valid display name THEN returns localised name`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.displayLanguage } returns LOCALISED_ENGLISH_CODE_NAME

            Assert.assertEquals(LOCALISED_ENGLISH_CODE_NAME, track.localizedLanguage)
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
            mockkStatic(THEOplayerGlobal::class)
            every { THEOplayerGlobal.getVersion() } returns args.playerVersion

            every { track.type } returns TextTrackType.CEA608
            every { track.label } returns args.label
            every { track.language } returns args.language
//            every { track.channelNumber } returns args.channelNumber

            mockkStatic(Track::localizedLanguage)
            every { any<Track>().localizedLanguage } returns args.localizedLanguageName
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
            val channelNumber: String?,
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
                    channelNumber = null,
                    playerVersion = "",
                    expectedLabel = null,
                ),

                // v10 checks.
                Args(
                    label = "Hello world",
                    language = null,
                    localizedLanguageName = null,
                    channelNumber = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "Hello world",
                ),
                Args(
                    label = null,
                    language = "en",
                    localizedLanguageName = "English",
                    channelNumber = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "English",
                ),
                Args(
                    label = "en",
                    language = "en",
                    localizedLanguageName = "English",
                    channelNumber = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "English",
                ),
                Args(
                    label = "en",
                    language = null,
                    localizedLanguageName = null,
                    channelNumber = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "en",
                ),
                Args(
                    label = "CC1",
                    language = "en",
                    localizedLanguageName = "English",
                    channelNumber = null,
                    playerVersion = TEST_PLAYER_VERSION_10,
                    expectedLabel = "English",
                ),

                // v11 checks.
                Args(
                    label = "Hello world",
                    language = null,
                    localizedLanguageName = null,
                    channelNumber = null,
                    playerVersion = TEST_PLAYER_VERSION_11,
                    expectedLabel = "Hello world",
                ),
                Args(
                    label = "en",
                    language = "en",
                    localizedLanguageName = "English",
                    channelNumber = null,
                    playerVersion = TEST_PLAYER_VERSION_11,
                    expectedLabel = "en",
                ),
            )
        }
    }
}
