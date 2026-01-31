package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.VoxeraColors

@Composable
fun VoxeraBackground(content: @Composable () -> Unit) {
  Box(modifier = Modifier.fillMaxSize()) {
    Image(
      painter = painterResource(R.drawable.bg_clean),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    content()
  }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "←",
      color = VoxeraColors.TextPrimary,
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier
        .padding(end = 10.dp)
        .pointerInput(Unit) {
          detectTapGestures(onTap = { onBack() })
        }
    )

    Text(
      text = title,
      color = Color(0xFF1E3A5F), // Темно-синий цвет
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.SemiBold
    )
  }
}
