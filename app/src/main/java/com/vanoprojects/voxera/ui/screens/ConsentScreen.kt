package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.ui.theme.VoxeraTheme
import com.vanoprojects.voxera.ui.theme.VoxeraColors

@Composable
fun ConsentScreen(
  onBack: () -> Unit,
  onAccept: () -> Unit
) {
  var agree1 by remember { mutableStateOf(false) }
  var agree2 by remember { mutableStateOf(false) }

  VoxeraBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        text = "Приватность и согласие",
        style = MaterialTheme.typography.headlineSmall,
        color = Color(0xFF1E3A5F), // Темно-синий цвет
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Voxera анализирует голосовые признаки для оценки текущего состояния. Это не медицинский диагноз.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF1E3A5F), // Темно-синий цвет
      )
      Spacer(modifier = Modifier.height(24.dp))

      ConsentRow(
        checked = agree1,
        onChecked = { agree1 = it },
        text = "Я согласен(а) на обработку голосовых данных для анализа"
      )
      Spacer(modifier = Modifier.height(12.dp))
      ConsentRow(
        checked = agree2,
        onChecked = { agree2 = it },
        text = "Я ознакомлен(а) с правилами приватности"
      )

      Spacer(modifier = Modifier.weight(1f))

      Button(
        onClick = onAccept,
        modifier = Modifier.fillMaxWidth(),
        enabled = agree1 && agree2,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = VoxeraColors.TextSecondary
        )
      ) {
        Text("Начать", color = VoxeraColors.TextPrimary)
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun ConsentRow(checked: Boolean, onChecked: (Boolean) -> Unit, text: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onChecked,
      colors = CheckboxDefaults.colors(
        checkedColor = VoxeraColors.TextSecondary
      )
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = text,
      color = Color(0xFF1E3A5F), // Темно-синий цвет
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f)
    )
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConsentScreenPreview() {
  VoxeraTheme {
    ConsentScreen(
      onBack = {},
      onAccept = {}
    )
  }
}
