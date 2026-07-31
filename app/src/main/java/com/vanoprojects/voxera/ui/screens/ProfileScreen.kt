package com.vanoprojects.voxera.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.vanoprojects.voxera.BuildConfig
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.CredentialStore
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.ThemedCard
import com.vanoprojects.voxera.ui.theme.TextWithShadow
import com.vanoprojects.voxera.ui.theme.cardParagraphTextStyle
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
  prefsManager: PreferencesManager
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  // Recompose when auth state changes after sign-in / sign-out
  var authEpoch by remember { mutableStateOf(0) }
  val currentUser = remember(authEpoch) { FirebaseAuth.getInstance().currentUser }
  val guest = currentUser == null

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

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = maxHeight)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (guest) {
          ThemedCard(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight(),
            gradientIndex = 0
          ) {
            AuthCardContent(
              prefsManager = prefsManager,
              onAuthComplete = { authEpoch++ },
              onSkip = {},
              showSkipButton = false
            )
          }
        } else {
          ThemedCard(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight(),
            gradientIndex = 0
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              TextWithShadow(
                text = strings.profile,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
              )
              TextWithShadow(
                text = currentUser!!.email
                  ?: currentUser.displayName
                  ?: strings.userName,
                style = cardParagraphTextStyle(),
                color = colors.textSecondary
              )
              Spacer(modifier = Modifier.height(4.dp))
              TextButton(
                onClick = {
                  scope.launch {
                    FirebaseAuth.getInstance().signOut()
                    val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                    if (!webClientId.isNullOrBlank()) {
                      val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()
                      GoogleSignIn.getClient(context as Activity, gso).signOut()
                    }
                    CredentialStore(context).clear()
                    prefsManager.setAuthCompleted(false)
                    prefsManager.setProfilePhotoPath(null)
                    prefsManager.setProfilePhone(null)
                    authEpoch++
                  }
                },
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(strings.profileSignOut, color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }
}
