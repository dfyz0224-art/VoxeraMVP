package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen(onBack: () -> Unit) {
  VoxeraBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      TopBar(title = "Помощь", onBack = onBack)
      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Как пользоваться:\n\n1) Выберите режим\n2) Дайте согласие\n3) Запишите 15–30 секунд голоса\n4) Получите карточку результата\n\nЕсли что-то пошло не так — попробуйте запись в более тихом месте.",
        color = Color(0xFF1E3A5F), // Темно-синий цвет
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}
