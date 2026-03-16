package com.theoplayer.android.ui.util

import com.theoplayer.android.api.player.track.Track
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
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
            Assert.assertNull(track.localisedLanguage)
        }

        @Test
        fun `GIVEN language is und THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_UNDEFINED
            Assert.assertNull(track.localisedLanguage)
        }

        @Test
        fun `GIVEN language is blank THEN localised language is null`() {
            every { track.language } returns TEST_BLANK_STRING
            Assert.assertNull(track.localisedLanguage)
        }

        @Test
        fun `GIVEN locale returns null as displayLanguage THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.displayLanguage } returns null

            Assert.assertNull(track.localisedLanguage)
        }

        @Test
        fun `GIVEN locale returns a blank string as displayLanguage THEN localised language is null`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.displayLanguage } returns TEST_BLANK_STRING

            Assert.assertNull(track.localisedLanguage)
        }

        @Test
        fun `GIVEN locale returns a valid display name THEN returns localised name`() {
            every { track.language } returns LANGUAGE_CODE_ENGLISH
            every { Locale.forLanguageTag(eq(LANGUAGE_CODE_ENGLISH)) } returns locale
            every { locale.displayLanguage } returns LOCALISED_ENGLISH_CODE_NAME

            Assert.assertEquals(LOCALISED_ENGLISH_CODE_NAME, track.localisedLanguage)
        }

        private companion object {
            const val LANGUAGE_CODE_UNDEFINED = "und"
            const val LANGUAGE_CODE_ENGLISH = "en"
            const val LOCALISED_ENGLISH_CODE_NAME = "English"
            const val TEST_BLANK_STRING = "     "
        }
    }
}