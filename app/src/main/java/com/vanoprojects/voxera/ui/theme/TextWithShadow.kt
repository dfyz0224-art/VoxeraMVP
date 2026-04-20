package com.vanoprojects.voxera.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TextWithShadow(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color,
    fontWeight: FontWeight? = null
) {
    val merged = style.copy(fontWeight = fontWeight ?: style.fontWeight)
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = merged,
            color = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 1.dp, y = 1.dp)
        )
        Text(
            text = text,
            style = merged,
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
