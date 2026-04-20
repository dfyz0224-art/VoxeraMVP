package com.vanoprojects.voxera.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.vanoprojects.voxera.BuildConfig
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthCardContent(
  prefsManager: PreferencesManager,
  onAuthComplete: () -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
  showSkipButton: Boolean = true
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isRegisterMode by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val auth = FirebaseAuth.getInstance()
  val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
  val showGoogleButton = !webClientId.isNullOrBlank()

  val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
      try {
        val account = task.getResult(ApiException::class.java)
        account?.idToken?.let { idToken ->
          scope.launch {
            isLoading = true
            errorMessage = null
            try {
              val credential = GoogleAuthProvider.getCredential(idToken, null)
              auth.signInWithCredential(credential).await()
              prefsManager.setAuthCompleted(true)
              onAuthComplete()
            } catch (e: Exception) {
              errorMessage = strings.authErrorGeneric
            } finally {
              isLoading = false
            }
          }
        }
      } catch (e: ApiException) {
        errorMessage = strings.authErrorGeneric
      }
    }
  }

  fun signInWithGoogle() {
    if (!showGoogleButton) return
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(webClientId)
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(context as Activity, gso)
    googleSignInLauncher.launch(client.signInIntent)
  }

  fun validateAndSubmit() {
    errorMessage = null
    when {
      email.isBlank() -> errorMessage = strings.authErrorInvalidEmail
      !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
        errorMessage = strings.authErrorInvalidEmail
      password.length < 6 -> errorMessage = strings.authErrorWeakPassword
      else -> {
        scope.launch {
          isLoading = true
          try {
            if (isRegisterMode) {
              auth.createUserWithEmailAndPassword(email, password).await()
            } else {
              auth.signInWithEmailAndPassword(email, password).await()
            }
            prefsManager.setAuthCompleted(true)
            onAuthComplete()
          } catch (e: Exception) {
            errorMessage = when ((e as? FirebaseAuthException)?.errorCode) {
              "invalid-email" -> strings.authErrorInvalidEmail
              "wrong-password" -> strings.authErrorWrongPassword
              "user-not-found" -> strings.authErrorUserNotFound
              "email-already-in-use" -> strings.authErrorEmailInUse
              "weak-password" -> strings.authErrorWeakPassword
              else -> strings.authErrorGeneric
            }
          } finally {
            isLoading = false
          }
        }
      }
    }
  }

  Box(modifier = modifier) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = email,
          onValueChange = { email = it; errorMessage = null },
          label = { Text(strings.authEmail) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primaryGlow,
            unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.5f),
            focusedLabelColor = colors.textPrimary,
            unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.primaryGlow,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
          )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; errorMessage = null },
          label = { Text(strings.authPassword) },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primaryGlow,
            unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.5f),
            focusedLabelColor = colors.textPrimary,
            unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.primaryGlow,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
          )
        )
        errorMessage?.let { msg ->
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = msg,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
        Spacer(modifier = Modifier.height(20.dp))
        ThemedFilledButton(
          text = if (isRegisterMode) strings.authRegister else strings.authLogin,
          onClick = { validateAndSubmit() },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
          onClick = { isRegisterMode = !isRegisterMode; errorMessage = null }
        ) {
          Text(
            text = if (isRegisterMode) strings.authLoginHint else strings.authRegisterHint,
            color = colors.primaryGlow
          )
        }
        if (showGoogleButton) {
          Spacer(modifier = Modifier.height(12.dp))
          GoogleSignInButton(
            text = strings.authGoogle,
            onClick = { signInWithGoogle() },
            modifier = Modifier.fillMaxWidth()
          )
        }
        if (showSkipButton) {
          Spacer(modifier = Modifier.height(12.dp))
          ThemedOutlinedButton(
            text = strings.authSkip,
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
          )
        }
    }
    if (isLoading) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          color = colors.primaryGlow,
          modifier = Modifier.size(48.dp)
        )
      }
    }
  }
}

@Composable
private fun GoogleSignInButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = colors.buttonText,
      containerColor = when (theme.type) {
        ThemeType.LIGHT -> colors.buttonBackground.copy(alpha = 0.5f)
        else -> colors.buttonBackground
      }
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (theme.type == ThemeType.LIGHT)
        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.08f)
      else colors.buttonBorder
    )
  ) {
    androidx.compose.foundation.Image(
      painter = androidx.compose.ui.res.painterResource(R.drawable.ic_google),
      contentDescription = null,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
  }
}
