package com.theoplayer.android.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Based on [androidx.compose.material3.IconButton]
 *  - Replaced `Modifier.size()` with `Modifier.defaultMinSize()`,
 *    to allow for larger icons.
 */
@Composable
internal fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = defaultIconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    TextButton(
        modifier = modifier
            .defaultMinSize(
                minWidth = IconButtonSize,
                minHeight = IconButtonSize
            ),
        shape = IconButtonDefaults.filledShape,
        enabled = enabled,
        colors = colors,
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource,
        onClick = onClick,
        content = { content() }
    )
}

private const val DisabledIconOpacity = 0.38f
private val IconButtonSize = 40.dp

/**
 * Equivalent to [IconButtonDefaults.iconButtonColors]
 */
@Composable
private fun defaultIconButtonColors(
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current,
    disabledContainerColor: Color = Color.Transparent,
    disabledContentColor: Color = contentColor.copy(alpha = DisabledIconOpacity)
): ButtonColors {
    return ButtonDefaults.textButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}