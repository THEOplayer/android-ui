package com.theoplayer.android.ui

import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import com.theoplayer.android.ui.util.THEOplayerGlobalExt
import com.theoplayer.android.ui.util.Version
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
class TextTrackStyleStateTest {
    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `seeds values from the text track style`() {
        mockPlayerVersion("9.9.0")
        val mock = MockPlayer()
        mock.textTrackStyle.fontColor = 0x80FF0000.toInt()
        mock.textTrackStyle.backgroundColor = 0x4000FF00
        mock.textTrackStyle.windowColor = 0x200000FF
        mock.textTrackStyle.edgeColor = 0x60FFFF00
        mock.textTrackStyle.edgeType = TextTrackStyle.EdgeType.EDGE_TYPE_OUTLINE
        mock.textTrackStyle.fontSize = 150

        val state = TextTrackStyleState(mock.player)

        assertEquals(Color(0x80FF0000.toInt()), state.fontColor)
        assertEquals(Color(0x4000FF00), state.backgroundColor)
        assertEquals(Color(0x200000FF), state.windowColor)
        assertEquals(Color(0x60FFFF00), state.edgeColor)
        assertEquals(TextTrackStyle.EdgeType.EDGE_TYPE_OUTLINE, state.edgeType)
        assertEquals(150, state.fontSize)
        assertEquals(50, state.fontOpacity)
        assertEquals(25, state.backgroundOpacity)
        assertEquals(13, state.windowOpacity)
        assertEquals(TextTrackFontFamily.DEFAULT, state.fontFamily)
    }

    @Test
    fun `default style has white text, black background and transparent window`() {
        mockPlayerVersion("9.9.0")
        val state = TextTrackStyleState(MockPlayer().player)

        assertEquals(Color(0xFFFFFFFF.toInt()), state.fontColor)
        assertEquals(Color(0xFF000000.toInt()), state.backgroundColor)
        assertEquals(Color(0x00000000), state.windowColor)
        assertEquals(100, state.fontOpacity)
        assertEquals(100, state.backgroundOpacity)
        assertEquals(0, state.windowOpacity)
        assertEquals(100, state.fontSize)
        assertEquals(TextTrackStyle.EdgeType.EDGE_TYPE_NONE, state.edgeType)
    }

    @Test
    fun `selectFontColor keeps the current opacity`() {
        mockPlayerVersion("9.9.0")
        val mock = MockPlayer()
        mock.textTrackStyle.fontColor = 0x80FFFFFF.toInt()
        val state = TextTrackStyleState(mock.player)

        state.selectFontColor(Color(0xFF00FF00))

        assertEquals(0x8000FF00.toInt(), mock.textTrackStyle.fontColor)
        assertEquals(Color(0x8000FF00.toInt()), state.fontColor)
    }

    @Test
    fun `selectFontOpacity keeps the current color`() {
        mockPlayerVersion("9.9.0")
        val mock = MockPlayer()
        mock.textTrackStyle.fontColor = 0xFF112233.toInt()
        val state = TextTrackStyleState(mock.player)

        state.selectFontOpacity(25)

        assertEquals(0x40112233, mock.textTrackStyle.fontColor)
        assertEquals(Color(0x40112233), state.fontColor)
        assertEquals(25, state.fontOpacity)
    }

    @Test
    fun `selectWindowOpacity keeps the current color`() {
        mockPlayerVersion("9.9.0")
        val mock = MockPlayer()
        mock.textTrackStyle.windowColor = 0xFF102030.toInt()
        val state = TextTrackStyleState(mock.player)

        state.selectWindowOpacity(75)

        assertEquals(0xBF102030.toInt(), mock.textTrackStyle.windowColor)
        assertEquals(75, state.windowOpacity)
    }

    @Test
    fun `selectBackgroundColor keeps the current opacity`() {
        mockPlayerVersion("9.9.0")
        val mock = MockPlayer()
        mock.textTrackStyle.backgroundColor = 0x40000000
        val state = TextTrackStyleState(mock.player)

        state.selectBackgroundColor(Color(0xFFFFFF00))

        assertEquals(0x40FFFF00, mock.textTrackStyle.backgroundColor)
        assertEquals(Color(0x40FFFF00), state.backgroundColor)
    }

    @Test
    fun `selectFontSize, selectEdgeType and selectEdgeColor write through`() {
        mockPlayerVersion("9.9.0")
        val mock = MockPlayer()
        val state = TextTrackStyleState(mock.player)

        state.selectFontSize(200)
        state.selectEdgeType(TextTrackStyle.EdgeType.EDGE_TYPE_DROP_SHADOW)
        state.selectEdgeColor(Color(0xFFFF00FF.toInt()))

        assertEquals(200, mock.textTrackStyle.fontSize)
        assertEquals(TextTrackStyle.EdgeType.EDGE_TYPE_DROP_SHADOW, mock.textTrackStyle.edgeType)
        assertEquals(0xFFFF00FF.toInt(), mock.textTrackStyle.edgeColor)
        assertEquals(200, state.fontSize)
        assertEquals(TextTrackStyle.EdgeType.EDGE_TYPE_DROP_SHADOW, state.edgeType)
        assertEquals(Color(0xFFFF00FF.toInt()), state.edgeColor)
    }

    @Test
    fun `reset restores the defaults but preserves the margins`() {
        mockPlayerVersion("9.9.0")
        val mock = MockPlayer()
        mock.textTrackStyle.apply {
            fontColor = 0x80FF0000.toInt()
            backgroundColor = 0x4000FF00
            edgeType = TextTrackStyle.EdgeType.EDGE_TYPE_RAISED
            fontSize = 200
            marginLeft = 5
            marginRight = 6
            marginTop = 7
            marginBottom = 8
        }
        val state = TextTrackStyleState(mock.player)

        state.reset()

        assertEquals(0xFFFFFFFF.toInt(), mock.textTrackStyle.fontColor)
        assertEquals(0xFF000000.toInt(), mock.textTrackStyle.backgroundColor)
        assertEquals(TextTrackStyle.EdgeType.EDGE_TYPE_NONE, mock.textTrackStyle.edgeType)
        assertEquals(100, mock.textTrackStyle.fontSize)
        assertEquals(5, mock.textTrackStyle.marginLeft)
        assertEquals(6, mock.textTrackStyle.marginRight)
        assertEquals(7, mock.textTrackStyle.marginTop)
        assertEquals(8, mock.textTrackStyle.marginBottom)
        assertEquals(Color(0xFFFFFFFF.toInt()), state.fontColor)
        assertEquals(100, state.fontSize)
    }

    @Test
    fun `selectFontFamily sets the font`() {
        mockPlayerVersion("9.9.0")
        mockTypeface()
        val mock = MockPlayer()
        val state = TextTrackStyleState(mock.player)

        state.selectFontFamily(TextTrackFontFamily.PROPORTIONAL_SERIF)

        assertTrue(mock.textTrackStyle.font === TextTrackFontFamily.PROPORTIONAL_SERIF.typeface)
        assertEquals(TextTrackFontFamily.PROPORTIONAL_SERIF, state.fontFamily)

        state.selectFontFamily(TextTrackFontFamily.DEFAULT)

        assertNull(mock.textTrackStyle.font)
        assertEquals(TextTrackFontFamily.DEFAULT, state.fontFamily)
    }

    @Test
    fun `fontFamily is null for a custom font`() {
        mockPlayerVersion("9.9.0")
        mockTypeface()
        val mock = MockPlayer()
        mock.textTrackStyle.font = mockk()
        val state = TextTrackStyleState(mock.player)

        assertNull(state.fontFamily)
    }

    @Test
    fun `font family is not supported below THEOplayer 9_9_0`() {
        mockPlayerVersion("9.8.0")
        val mock = MockPlayer()
        val state = TextTrackStyleState(mock.player)

        assertFalse(state.isFontFamilySupported)
        state.selectFontFamily(TextTrackFontFamily.PROPORTIONAL_SERIF)
        verify(exactly = 0) { mock.textTrackStyle.style.setFont(any<Typeface>()) }
        assertEquals(TextTrackFontFamily.DEFAULT, state.fontFamily)
    }

    @Test
    fun `null player gives defaults and no crash`() {
        val state = TextTrackStyleState(null)

        assertFalse(state.isFontFamilySupported)
        assertEquals(TextTrackFontFamily.DEFAULT, state.fontFamily)
        assertEquals(Color(0xFFFFFFFF.toInt()), state.fontColor)
        assertEquals(Color(0xFF000000.toInt()), state.backgroundColor)
        assertEquals(Color(0x00000000), state.windowColor)
        assertEquals(Color(0xFFFFFFFF.toInt()), state.edgeColor)
        assertEquals(100, state.fontSize)
        assertEquals(TextTrackStyle.EdgeType.EDGE_TYPE_NONE, state.edgeType)

        state.selectFontColor(Color(0xFF00FF00))
        state.selectFontOpacity(50)
        state.selectBackgroundColor(Color(0xFF00FF00))
        state.selectBackgroundOpacity(50)
        state.selectWindowColor(Color(0xFF00FF00))
        state.selectWindowOpacity(50)
        state.selectEdgeColor(Color(0xFF00FF00))
        state.selectFontSize(200)
        state.selectEdgeType(TextTrackStyle.EdgeType.EDGE_TYPE_OUTLINE)
        state.selectFontFamily(TextTrackFontFamily.PROPORTIONAL_SERIF)
        state.reset()
    }

    private fun mockPlayerVersion(version: String) {
        mockkObject(THEOplayerGlobalExt)
        every { THEOplayerGlobalExt.version } returns Version.parse(version)
    }

    private fun mockTypeface() {
        mockkStatic(Typeface::class)
        every { Typeface.create(any<String>(), any()) } answers
            {
                mockk<Typeface>(name = firstArg())
            }
    }
}
