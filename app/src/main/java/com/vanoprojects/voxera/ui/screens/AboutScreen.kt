package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
  VoxeraBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      TopBar(title = "О приложении", onBack = onBack)
      Spacer(modifier = Modifier.height(30.dp))
      Image(
        painter = painterResource(R.drawable.ic_voxera_x_glow),
        contentDescription = null,
        modifier = Modifier.size(96.dp)
      )
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        "Voxera",
        color = Color(0xFF1E3A5F), // Темно-синий цвет
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "Экспериментальный анализ состояния по голосу. Не является медицинским диагнозом.",
        color = Color(0xFF1E3A5F), // Темно-синий цвет
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}
