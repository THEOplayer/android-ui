package com.theoplayer.android.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun MenuScope.LanguageMenuButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.language),
            tint = Color.White,
            contentDescription = "Language"
        )
    }
) {
    IconButton(
        modifier = modifier,
        onClick = { openMenu { LanguageMenu() } }) {
        content()
    }
}