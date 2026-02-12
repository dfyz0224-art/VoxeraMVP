package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.ThemeType

@Composable
fun VoxeraBackground(content: @Composable () -> Unit) {
  val theme = LocalVoxeraTheme.current
  
  Box(modifier = Modifier.fillMaxSize()) {
    Image(
      painter = painterResource(
        if (theme.type == ThemeType.GLASS) {
          R.drawable.bg_stars
        } else {
          R.drawable.bg_clean
        }
      ),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    content()
  }
}

@Composable
fun TopBar(title: String) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  Text(
    text = title,
    color = colors.backgroundTextPrimary,
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.SemiBold
  )
}
