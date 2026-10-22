package com.theoplayer.android.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import com.theoplayer.android.ui.common.R as CommonR
import java.text.NumberFormat

/**
 * An option in a text track style menu, such as [TextTrackStyleMenu].
 *
 * Describes the selectable [values] of a single style property, and how to read, match, format and
 * apply them on a [TextTrackStyleState].
 *
 * @param T the type of the option's values
 */
@InternalUiApi
@ExperimentalPlayerStateApi
class TextTrackStyleOption<T>
internal constructor(
    /** The resource ID of the option's label. */
    @StringRes val label: Int,
    /** The values that can be selected for this option. */
    val values: List<T>,
    private val current: (TextTrackStyleState) -> T,
    private val matches: (T, T) -> Boolean,
    private val apply: (TextTrackStyleState, T) -> Unit,
    private val format: @Composable (T) -> String,
    private val color: ((T) -> Color)? = null,
    private val available: (TextTrackStyleState) -> Boolean = { true },
) {
    /** Whether this option can be shown for the given [state]. */
    fun isAvailable(state: TextTrackStyleState): Boolean = available(state)

    /** Whether [value] is the state's current value for this option. */
    fun isSelected(state: TextTrackStyleState, value: T): Boolean = matches(current(state), value)

    /** Applies [value] to the [state]. */
    fun select(state: TextTrackStyleState, value: T) = apply(state, value)

    /** Formats [value] for display. */
    @Composable fun formatValue(value: T): String = format(value)

    /** Formats the state's current value for this option. */
    @Composable fun formatCurrentValue(state: TextTrackStyleState): String = format(current(state))

    /** The color to show next to [value], or `null` if this isn't a color option. */
    fun colorOf(value: T): Color? = color?.invoke(value)
}

private val textTrackStyleColors =
    listOf(
        Color(0xFFFFFFFF) to CommonR.string.theoplayer_ui_color_white,
        Color(0xFFFFFF00) to CommonR.string.theoplayer_ui_color_yellow,
        Color(0xFF00FF00) to CommonR.string.theoplayer_ui_color_green,
        Color(0xFF00FFFF) to CommonR.string.theoplayer_ui_color_cyan,
        Color(0xFF0000FF) to CommonR.string.theoplayer_ui_color_blue,
        Color(0xFFFF00FF) to CommonR.string.theoplayer_ui_color_magenta,
        Color(0xFFFF0000) to CommonR.string.theoplayer_ui_color_red,
        Color(0xFF000000) to CommonR.string.theoplayer_ui_color_black,
    )

private val textTrackStyleFontSizes = listOf(50, 75, 100, 150, 200)
private val textTrackStyleFontOpacities = listOf(25, 50, 75, 100)
private val textTrackStyleBackgroundOpacities = listOf(0, 25, 50, 75, 100)

private val textTrackStyleEdgeTypes =
    listOf(
        TextTrackStyle.EdgeType.EDGE_TYPE_NONE,
        TextTrackStyle.EdgeType.EDGE_TYPE_DROP_SHADOW,
        TextTrackStyle.EdgeType.EDGE_TYPE_RAISED,
        TextTrackStyle.EdgeType.EDGE_TYPE_DEPRESSED,
        TextTrackStyle.EdgeType.EDGE_TYPE_OUTLINE,
    )

private fun Color.rgbEquals(other: Color): Boolean =
    (toArgb() and 0x00FFFFFF) == (other.toArgb() and 0x00FFFFFF)

@Composable
private fun formatTextTrackColor(color: Color): String {
    return textTrackStyleColors
        .firstOrNull { it.first.rgbEquals(color) }
        ?.let { stringResource(it.second) } ?: stringResource(CommonR.string.theoplayer_ui_custom)
}

@Composable
private fun formatTextTrackFontFamily(fontFamily: TextTrackFontFamily?): String {
    return when (fontFamily) {
        TextTrackFontFamily.DEFAULT ->
            stringResource(CommonR.string.theoplayer_ui_font_family_default)
        TextTrackFontFamily.MONOSPACE_SERIF ->
            stringResource(CommonR.string.theoplayer_ui_font_family_monospace_serif)
        TextTrackFontFamily.PROPORTIONAL_SERIF ->
            stringResource(CommonR.string.theoplayer_ui_font_family_proportional_serif)
        TextTrackFontFamily.MONOSPACE_SANS ->
            stringResource(CommonR.string.theoplayer_ui_font_family_monospace_sans)
        TextTrackFontFamily.PROPORTIONAL_SANS ->
            stringResource(CommonR.string.theoplayer_ui_font_family_proportional_sans)
        null -> stringResource(CommonR.string.theoplayer_ui_custom)
    }
}

@Composable
private fun formatTextTrackEdgeType(edgeType: TextTrackStyle.EdgeType): String {
    return when (edgeType) {
        TextTrackStyle.EdgeType.EDGE_TYPE_NONE ->
            stringResource(CommonR.string.theoplayer_ui_edge_type_none)
        TextTrackStyle.EdgeType.EDGE_TYPE_OUTLINE ->
            stringResource(CommonR.string.theoplayer_ui_edge_type_uniform)
        TextTrackStyle.EdgeType.EDGE_TYPE_DROP_SHADOW ->
            stringResource(CommonR.string.theoplayer_ui_edge_type_drop_shadow)
        TextTrackStyle.EdgeType.EDGE_TYPE_RAISED ->
            stringResource(CommonR.string.theoplayer_ui_edge_type_raised)
        TextTrackStyle.EdgeType.EDGE_TYPE_DEPRESSED ->
            stringResource(CommonR.string.theoplayer_ui_edge_type_depressed)
    }
}

@Composable
private fun formatPercentage(percent: Int): String {
    return NumberFormat.getPercentInstance().format(percent / 100.0)
}

private val fontFamilyOption =
    TextTrackStyleOption<TextTrackFontFamily?>(
        label = CommonR.string.theoplayer_ui_text_track_style_font_family,
        values = TextTrackFontFamily.entries,
        current = { it.fontFamily },
        matches = { current, value -> current == value },
        apply = { state, value -> value?.let(state::selectFontFamily) },
        format = { formatTextTrackFontFamily(it) },
        available = { it.isFontFamilySupported },
    )

private val fontColorOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_font_color,
        values = textTrackStyleColors.map { it.first },
        current = { it.fontColor },
        matches = { current, value -> current.rgbEquals(value) },
        apply = { state, value -> state.selectFontColor(value) },
        format = { formatTextTrackColor(it) },
        color = { it },
    )

private val fontSizeOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_font_size,
        values = textTrackStyleFontSizes,
        current = { it.fontSize },
        matches = { current, value -> current == value },
        apply = { state, value -> state.selectFontSize(value) },
        format = { formatPercentage(it) },
    )

private val fontOpacityOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_font_opacity,
        values = textTrackStyleFontOpacities,
        current = { it.fontOpacity },
        matches = { current, value -> current == value },
        apply = { state, value -> state.selectFontOpacity(value) },
        format = { formatPercentage(it) },
    )

private val edgeTypeOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_edge_type,
        values = textTrackStyleEdgeTypes,
        current = { it.edgeType },
        matches = { current, value -> current == value },
        apply = { state, value -> state.selectEdgeType(value) },
        format = { formatTextTrackEdgeType(it) },
    )

private val edgeColorOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_edge_color,
        values = textTrackStyleColors.map { it.first },
        current = { it.edgeColor },
        matches = { current, value -> current.rgbEquals(value) },
        apply = { state, value -> state.selectEdgeColor(value) },
        format = { formatTextTrackColor(it) },
        color = { it },
    )

private val backgroundColorOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_background_color,
        values = textTrackStyleColors.map { it.first },
        current = { it.backgroundColor },
        matches = { current, value -> current.rgbEquals(value) },
        apply = { state, value -> state.selectBackgroundColor(value) },
        format = { formatTextTrackColor(it) },
        color = { it },
    )

private val backgroundOpacityOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_background_opacity,
        values = textTrackStyleBackgroundOpacities,
        current = { it.backgroundOpacity },
        matches = { current, value -> current == value },
        apply = { state, value -> state.selectBackgroundOpacity(value) },
        format = { formatPercentage(it) },
    )

private val windowColorOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_window_color,
        values = textTrackStyleColors.map { it.first },
        current = { it.windowColor },
        matches = { current, value -> current.rgbEquals(value) },
        apply = { state, value -> state.selectWindowColor(value) },
        format = { formatTextTrackColor(it) },
        color = { it },
    )

private val windowOpacityOption =
    TextTrackStyleOption(
        label = CommonR.string.theoplayer_ui_text_track_style_window_opacity,
        values = textTrackStyleBackgroundOpacities,
        current = { it.windowOpacity },
        matches = { current, value -> current == value },
        apply = { state, value -> state.selectWindowOpacity(value) },
        format = { formatPercentage(it) },
    )

/** The options of a text track style menu, in display order. */
@InternalUiApi
@ExperimentalPlayerStateApi
val textTrackStyleOptions: List<TextTrackStyleOption<*>> =
    listOf(
        fontFamilyOption,
        fontColorOption,
        fontOpacityOption,
        fontSizeOption,
        backgroundColorOption,
        backgroundOpacityOption,
        windowColorOption,
        windowOpacityOption,
        edgeTypeOption,
        edgeColorOption,
    )
