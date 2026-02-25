package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun AboutScreen() {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  
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
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      Spacer(modifier = Modifier.height(30.dp))
      Image(
        painter = painterResource(R.drawable.ic_voxera_x_glow),
        contentDescription = null,
        modifier = Modifier.size(96.dp),
        colorFilter = if (theme.type == ThemeType.LIGHT) {
          ColorFilter.tint(colors.backgroundTextPrimary)
        } else null
      )
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        "Voxera",
        color = colors.backgroundTextPrimary,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        strings.aboutDescription,
        color = colors.backgroundTextSecondary,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}
