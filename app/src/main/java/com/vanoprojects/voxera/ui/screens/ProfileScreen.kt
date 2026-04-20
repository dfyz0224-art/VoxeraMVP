package com.vanoprojects.voxera.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.BuildConfig
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

/** Сдвиг иконки по горизонтали на карточках подписок */
private val subscriptionCardIconOffsetX = (-20).dp

@Composable
fun ProfileScreen(
  prefsManager: PreferencesManager,
  onForBusiness: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val firebaseUser = FirebaseAuth.getInstance().currentUser
  val isGuest = firebaseUser == null
  val profilePhotoPath by prefsManager.profilePhotoPath.collectAsState(initial = null)
  val profilePhone by prefsManager.profilePhone.collectAsState(initial = null)
  var showEditDialog by remember { mutableStateOf(false) }
  var showAuthCard by remember { mutableStateOf(false) }

  val displayName = firebaseUser?.displayName ?: firebaseUser?.email?.substringBefore("@") ?: strings.userName
  val email = firebaseUser?.email ?: strings.profileEmailValue
  val phone = profilePhone ?: firebaseUser?.phoneNumber ?: strings.profilePhoneValue
  val photoUrl = firebaseUser?.photoUrl?.toString()
  val hasCustomPhoto = !profilePhotoPath.isNullOrBlank()
  val photoToShow = when {
    hasCustomPhoto -> "file://${profilePhotoPath}"
    !photoUrl.isNullOrBlank() -> photoUrl
    else -> null
  }

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
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      if (isGuest) {
        ThemedCard(
          gradientIndex = 0,
          height = 280.dp,
          onClick = { showAuthCard = true }
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = strings.profileGuestTitle,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
              modifier = Modifier.padding(vertical = 24.dp)
            )
            Image(
              painter = painterResource(R.drawable.ic_profile),
              contentDescription = null,
              modifier = Modifier.size(56.dp).clip(CircleShape),
              colorFilter = ColorFilter.tint(colors.textPrimary)
            )
          }
        }
        if (showAuthCard) {
          Spacer(modifier = Modifier.height(16.dp))
          ThemedCard(modifier = Modifier.wrapContentHeight(), gradientIndex = 1) {
            AuthCardContent(
              prefsManager = prefsManager,
              onAuthComplete = { showAuthCard = false },
              onSkip = { showAuthCard = false },
              showSkipButton = true
            )
          }
        }
      } else {
        ThemedCard(
          gradientIndex = 0,
          height = 250.dp,
          onClick = { showEditDialog = true }
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            ProfilePhoto(
              photoUrl = photoToShow,
              modifier = Modifier.size(56.dp),
              colors = colors
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.Start,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              ProfileRow(label = strings.profileNameLabel, value = displayName)
              ProfileRow(label = strings.profilePhoneLabel, value = phone)
              ProfileRow(label = strings.profileEmailLabel, value = email)
              ProfileRow(label = strings.profilePaymentLabel, value = strings.profilePaymentValue)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
      Text(
        text = strings.manageSubscriptions,
        style = MaterialTheme.typography.titleMedium,
        color = colors.textPrimary,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(modifier = Modifier.width(280.dp)) {
          SubscriptionCard(
            title = strings.planBasic,
            description = strings.planBasicDesc,
            isCurrent = true,
            gradientIndex = 0,
            onClick = {}
          )
        }
        Box(modifier = Modifier.width(280.dp)) {
          SubscriptionCard(
            title = strings.planStandard,
            description = strings.planStandardDesc,
            isCurrent = false,
            gradientIndex = 1,
            onClick = {}
          )
        }
        Box(modifier = Modifier.width(280.dp)) {
          SubscriptionCard(
            title = strings.planPro,
            description = strings.planProDesc,
            isCurrent = false,
            gradientIndex = 2,
            onClick = {}
          )
        }
        Box(modifier = Modifier.width(280.dp)) {
          SubscriptionCard(
            title = strings.planUnlimited,
            description = strings.planUnlimitedDesc,
            isCurrent = false,
            gradientIndex = 3,
            onClick = {}
          )
        }
        Box(modifier = Modifier.width(280.dp)) {
          SubscriptionCard(
            title = strings.planBusiness,
            description = strings.planBusinessDesc,
            isCurrent = false,
            gradientIndex = 4,
            onClick = onForBusiness
          )
        }
      }
    }
  }

  if (showEditDialog && !isGuest) {
    EditProfileDialog(
      prefsManager = prefsManager,
      currentPhotoPath = profilePhotoPath,
      currentPhone = profilePhone,
      onDismiss = { showEditDialog = false }
    )
  }
}

@Composable
private fun ProfilePhoto(
  photoUrl: String?,
  modifier: Modifier,
  colors: com.vanoprojects.voxera.ui.theme.ThemeColors
) {
  if (photoUrl != null) {
    AsyncImage(
      model = photoUrl,
      contentDescription = null,
      modifier = modifier.clip(CircleShape)
    )
  } else {
    Image(
      painter = painterResource(R.drawable.ic_profile),
      contentDescription = null,
      modifier = modifier.clip(CircleShape),
      colorFilter = ColorFilter.tint(colors.textPrimary)
    )
  }
}

@Composable
private fun EditProfileDialog(
  prefsManager: PreferencesManager,
  currentPhotoPath: String?,
  currentPhone: String?,
  onDismiss: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var phone by remember { mutableStateOf(currentPhone ?: "") }
  var photoPath by remember { mutableStateOf(currentPhotoPath) }

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    uri?.let {
      val file = File(context.filesDir, "profile_photo.jpg")
      context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output ->
          input.copyTo(output)
        }
      }
      scope.launch {
        prefsManager.setProfilePhotoPath(file.absolutePath)
        photoPath = file.absolutePath
      }
    }
  }

  AlertDialog(
    onDismissRequest = {
      scope.launch {
        prefsManager.setProfilePhone(phone.ifBlank { null })
      }
      onDismiss()
    },
    title = { Text(strings.profileEditTitle) },
    confirmButton = {
      TextButton(onClick = {
        scope.launch {
          prefsManager.setProfilePhone(phone.ifBlank { null })
        }
        onDismiss()
      }) {
        Text(strings.profileDone, color = colors.primaryGlow)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        OutlinedButton(
          onClick = {
            photoPicker.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(strings.profileChangePhoto)
        }
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(strings.profilePhoneLabel) },
          singleLine = true,
          keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = KeyboardType.Phone
          ),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
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
              prefsManager.setAuthCompleted(false)
              prefsManager.setProfilePhotoPath(null)
              prefsManager.setProfilePhone(null)
              onDismiss()
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(strings.profileSignOut, color = MaterialTheme.colorScheme.error)
        }
      }
    }
  )
}

@Composable
private fun ProfileRow(label: String, value: String) {
  val colors = LocalVoxeraTheme.current.colors
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "$label: ",
      style = MaterialTheme.typography.bodyMedium,
      color = colors.textSecondary
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = colors.textPrimary,
      fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    )
  }
}

@Composable
private fun SubscriptionCard(
  title: String,
  description: String,
  isCurrent: Boolean,
  gradientIndex: Int,
  onClick: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current

  ThemedCard(
    gradientIndex = gradientIndex,
    onClick = onClick,
    contentPadding = PaddingValues(vertical = 14.dp, horizontal = 30.dp)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Image(
        painter = painterResource(R.drawable.ic_x_white_glow),
        contentDescription = null,
        modifier = Modifier
          .size(128.dp)
          .offset(x = subscriptionCardIconOffsetX)
          .align(Alignment.CenterStart)
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()
          .padding(start = 130.dp + subscriptionCardIconOffsetX)
          .verticalScroll(rememberScrollState())
          .align(Alignment.TopStart)
      ) {
        if (isCurrent) {
          Text(
            text = strings.currentPlan,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
          fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = description,
          style = cardParagraphSmallTextStyle(),
          color = colors.textSecondary,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
