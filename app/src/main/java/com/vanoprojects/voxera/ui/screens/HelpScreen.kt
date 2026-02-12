package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun HelpScreen() {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  Box(modifier = Modifier.fillMaxSize()) {
    if (theme.type == ThemeType.LIGHT) {
      Image(
        painter = painterResource(R.drawable.bg_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      VoxeraBackground {}
    }
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      TopBar(title = "Помощь")
      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Как пользоваться:\n\n1) Выберите режим\n2) Дайте согласие\n3) Запишите 15–30 секунд голоса\n4) Получите карточку результата\n\nЕсли что-то пошло не так — попробуйте запись в более тихом месте.",
        color = colors.backgroundTextPrimary,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}
