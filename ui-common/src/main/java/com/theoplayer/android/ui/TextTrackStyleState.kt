package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import com.theoplayer.android.ui.util.fontCompat
import com.theoplayer.android.ui.util.isTextTrackStyleFontSupported
import com.theoplayer.android.ui.util.marginBottomCompat
import kotlin.math.roundToInt

private val defaultFontColor = Color(0xFFFFFFFF)
private val defaultBackgroundColor = Color(0xFF000000)
private val defaultWindowColor = Color(0x00000000)
private val defaultEdgeColor = Color(0xFFFFFFFF)
private const val defaultFontSize = 100
private val defaultEdgeType = TextTrackStyle.EdgeType.EDGE_TYPE_NONE

/**
 * State holder for the style of subtitles, such as [TextTrackStyleMenu].
 *
 * Create an instance with [rememberTextTrackStyleState].
 */
@ExperimentalPlayerStateApi
class TextTrackStyleState internal constructor(private val player: Player?) {
    /**
     * Whether the font family can be changed.
     *
     * Changing the font family requires THEOplayer 9.9.0 or higher.
     */
    val isFontFamilySupported: Boolean
        get() = player != null && isTextTrackStyleFontSupported

    /**
     * The font family of the subtitles.
     *
     * [TextTrackFontFamily.DEFAULT] if no custom font is set, or `null` if a font not in
     * [TextTrackFontFamily] is set.
     */
    var fontFamily by mutableStateOf<TextTrackFontFamily?>(TextTrackFontFamily.DEFAULT)
        private set

    /** The color of the subtitle text, including its opacity. */
    var fontColor by mutableStateOf(defaultFontColor)
        private set

    /** The color of the background directly behind the subtitle text, including its opacity. */
    var backgroundColor by mutableStateOf(defaultBackgroundColor)
        private set

    /** The color of the window behind the subtitle text, including its opacity. */
    var windowColor by mutableStateOf(defaultWindowColor)
        private set

    /** The color of the edge of the subtitle text, including its opacity. */
    var edgeColor by mutableStateOf(defaultEdgeColor)
        private set

    /** The opacity of the subtitle text, as a percentage. */
    val fontOpacity: Int
        get() = fontColor.opacityPercent

    /** The opacity of the background directly behind the subtitle text, as a percentage. */
    val backgroundOpacity: Int
        get() = backgroundColor.opacityPercent

    /** The opacity of the window behind the subtitle text, as a percentage. */
    val windowOpacity: Int
        get() = windowColor.opacityPercent

    /** The font size of the subtitles, as a percentage of the default font size. */
    var fontSize by mutableIntStateOf(defaultFontSize)
        private set

    /** The edge style of the subtitle text. */
    var edgeType by mutableStateOf(defaultEdgeType)
        private set

    private fun update() {
        val style = player?.textTrackStyle
        fontFamily = if (style != null) fontFamilyOf(style) else TextTrackFontFamily.DEFAULT
        fontColor = style?.let { Color(it.fontColor) } ?: defaultFontColor
        backgroundColor = style?.let { Color(it.backgroundColor) } ?: defaultBackgroundColor
        windowColor = style?.let { Color(it.windowColor) } ?: defaultWindowColor
        edgeColor = style?.let { Color(it.edgeColor) } ?: defaultEdgeColor
        fontSize = style?.fontSize ?: defaultFontSize
        edgeType = style?.edgeType ?: defaultEdgeType
    }

    init {
        update()
    }

    /**
     * Selects the font family of the subtitles.
     *
     * Does nothing if the font family can't be changed, see [isFontFamilySupported].
     */
    fun selectFontFamily(fontFamily: TextTrackFontFamily) {
        val style = player?.textTrackStyle ?: return
        if (!isTextTrackStyleFontSupported) return
        style.fontCompat = fontFamily.typeface
        this.fontFamily = fontFamily
    }

    /** Selects the color of the subtitle text, keeping the current [font opacity][fontOpacity]. */
    fun selectFontColor(color: Color) {
        selectColor(color.copy(alpha = fontColor.alpha), fontColorSetter)
    }

    /**
     * Selects the color of the background directly behind the subtitle text, keeping the current
     * [background opacity][backgroundOpacity].
     */
    fun selectBackgroundColor(color: Color) {
        selectColor(color.copy(alpha = backgroundColor.alpha), backgroundColorSetter)
    }

    /**
     * Selects the color of the window behind the subtitle text, keeping the current
     * [window opacity][windowOpacity].
     */
    fun selectWindowColor(color: Color) {
        selectColor(color.copy(alpha = windowColor.alpha), windowColorSetter)
    }

    /** Selects the color of the edge of the subtitle text, keeping its current opacity. */
    fun selectEdgeColor(color: Color) {
        selectColor(color.copy(alpha = edgeColor.alpha), edgeColorSetter)
    }

    /** Selects the opacity of the subtitle text, keeping the current [font color][fontColor]. */
    fun selectFontOpacity(opacity: Int) {
        selectColor(fontColor.withOpacity(opacity), fontColorSetter)
    }

    /**
     * Selects the opacity of the background directly behind the subtitle text, keeping the current
     * [background color][backgroundColor].
     */
    fun selectBackgroundOpacity(opacity: Int) {
        selectColor(backgroundColor.withOpacity(opacity), backgroundColorSetter)
    }

    /**
     * Selects the opacity of the window behind the subtitle text, keeping the current
     * [window color][windowColor].
     */
    fun selectWindowOpacity(opacity: Int) {
        selectColor(windowColor.withOpacity(opacity), windowColorSetter)
    }

    /** Selects the font size of the subtitles, as a percentage of the default font size. */
    fun selectFontSize(fontSize: Int) {
        val style = player?.textTrackStyle ?: return
        style.fontSize = fontSize
        this.fontSize = fontSize
    }

    /** Selects the edge style of the subtitle text. */
    fun selectEdgeType(edgeType: TextTrackStyle.EdgeType) {
        val style = player?.textTrackStyle ?: return
        style.edgeType = edgeType
        this.edgeType = edgeType
    }

    /**
     * Resets the style to the built-in defaults.
     *
     * The margins of the subtitle drawing box are preserved, since these are managed by the
     * player's user interface.
     */
    fun reset() {
        val style = player?.textTrackStyle ?: return
        val marginLeft = style.marginLeft
        val marginRight = style.marginRight
        val marginTop = style.marginTop
        val marginBottom = style.marginBottomCompat
        style.reset()
        style.marginLeft = marginLeft
        style.marginRight = marginRight
        style.marginTop = marginTop
        if (marginBottom != null) {
            style.marginBottomCompat = marginBottom
        }
        update()
    }

    private fun selectColor(color: Color, setter: ColorSetter) {
        val style = player?.textTrackStyle ?: return
        setter(style, color)
        update()
    }

    private fun fontFamilyOf(style: TextTrackStyle): TextTrackFontFamily? {
        val font = style.fontCompat ?: return TextTrackFontFamily.DEFAULT
        return TextTrackFontFamily.entries.firstOrNull { it.typeface == font }
    }

    private val Color.opacityPercent: Int
        get() = (alpha * 100).roundToInt()

    private fun Color.withOpacity(opacity: Int): Color =
        Color((toArgb() and 0x00FFFFFF) or ((opacity * 255 / 100f).roundToInt() shl 24))
}

private typealias ColorSetter = (TextTrackStyle, Color) -> Unit

private val fontColorSetter: ColorSetter = { style, color -> style.fontColor = color.toArgb() }
private val backgroundColorSetter: ColorSetter = { style, color ->
    style.backgroundColor = color.toArgb()
}
private val windowColorSetter: ColorSetter = { style, color -> style.windowColor = color.toArgb() }
private val edgeColorSetter: ColorSetter = { style, color -> style.edgeColor = color.toArgb() }

/** Creates a [TextTrackStyleState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberTextTrackStyleState(player: Player?): TextTrackStyleState {
    return remember(player) { TextTrackStyleState(player) }
}
