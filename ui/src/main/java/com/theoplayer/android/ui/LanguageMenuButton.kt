package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MenuScope.LanguageMenuButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
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
        contentPadding = contentPadding,
        onClick = { openMenu { LanguageMenu() } }) {
        content()
    }
}