package com.vanoprojects.voxera.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.vanoprojects.voxera.BuildConfig
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.CredentialStore
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.data.isAllowedIntoApp
import com.vanoprojects.voxera.data.requiresEmailVerification
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.strings.Strings
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.ThemedFilledButton
import com.vanoprojects.voxera.ui.theme.ThemedOutlinedButton
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG_GOOGLE_AUTH = "VoxeraGoogleAuth"

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
  val credentialStore = remember { CredentialStore(context) }

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordConfirm by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var passwordConfirmVisible by remember { mutableStateOf(false) }
  var rememberPassword by remember { mutableStateOf(false) }
  var isRegisterMode by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var infoMessage by remember { mutableStateOf<String?>(null) }
  var showResendVerification by remember { mutableStateOf(false) }
  var autoLoginAttempted by remember { mutableStateOf(false) }

  val auth = FirebaseAuth.getInstance()
  val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
  val showGoogleButton = !webClientId.isNullOrBlank()

  val fieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = colors.primaryGlow,
    unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.5f),
    focusedLabelColor = colors.textPrimary,
    unfocusedLabelColor = colors.textSecondary,
    cursorColor = colors.primaryGlow,
    focusedTextColor = colors.textPrimary,
    unfocusedTextColor = colors.textPrimary
  )

  LaunchedEffect(Unit) {
    rememberPassword = credentialStore.isRememberEnabled()
    credentialStore.load()?.let { (savedEmail, savedPassword) ->
      email = savedEmail
      password = savedPassword
    }
  }

  fun persistCredentialsAfterSuccess() {
    if (rememberPassword && email.isNotBlank() && password.isNotBlank()) {
      credentialStore.save(email, password)
    } else {
      credentialStore.clear()
    }
  }

  suspend fun finishIfAllowed() {
    val user = auth.currentUser ?: throw IllegalStateException("No user")
    user.reload().await()
    if (user.requiresEmailVerification()) {
      showResendVerification = true
      auth.signOut()
      errorMessage = strings.authEmailNotVerified
      return
    }
    showResendVerification = false
    persistCredentialsAfterSuccess()
    prefsManager.setAuthCompleted(true)
    onAuthComplete()
  }

  suspend fun signInWithEmailPassword(emailValue: String, passwordValue: String, register: Boolean) {
    if (register) {
      auth.createUserWithEmailAndPassword(emailValue, passwordValue).await()
      auth.currentUser?.sendEmailVerification()?.await()
      auth.signOut()
    } else {
      auth.signInWithEmailAndPassword(emailValue, passwordValue).await()
    }
  }

  LaunchedEffect(rememberPassword, isRegisterMode) {
    if (autoLoginAttempted || isRegisterMode || !rememberPassword) return@LaunchedEffect
    val saved = credentialStore.load() ?: return@LaunchedEffect
    email = saved.first
    password = saved.second
    if (password.length < 6) return@LaunchedEffect
    autoLoginAttempted = true
    isLoading = true
    errorMessage = null
    infoMessage = null
    try {
      signInWithEmailPassword(saved.first, saved.second, register = false)
      finishIfAllowed()
    } catch (_: Exception) {
      credentialStore.clear()
      rememberPassword = false
      password = ""
    } finally {
      isLoading = false
    }
  }

  fun finishGoogleSignIn(idToken: String) {
    scope.launch {
      isLoading = true
      errorMessage = null
      infoMessage = null
      try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        if (!rememberPassword) credentialStore.clear()
        prefsManager.setAuthCompleted(true)
        onAuthComplete()
      } catch (e: Exception) {
        Log.e(TAG_GOOGLE_AUTH, "Firebase signInWithCredential failed", e)
        errorMessage = strings.authErrorGeneric
      } finally {
        isLoading = false
      }
    }
  }

  val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    // Always parse the intent — Google may return account data even when resultCode != OK.
    val data = result.data
    if (data == null) {
      if (result.resultCode != Activity.RESULT_CANCELED) {
        Log.w(TAG_GOOGLE_AUTH, "Google Sign-In: empty data, resultCode=${result.resultCode}")
        errorMessage = strings.authErrorGeneric
      }
      return@rememberLauncherForActivityResult
    }
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
      val account = task.getResult(ApiException::class.java)
      val idToken = account?.idToken
      if (idToken.isNullOrBlank()) {
        // Typical on Play builds when App Signing SHA-1 is missing in Firebase —
        // account picker succeeds but idToken is null and UI used to silently reset.
        Log.e(
          TAG_GOOGLE_AUTH,
          "Google Sign-In: idToken is null (check Firebase SHA-1 for Play App Signing key; " +
            "webClientId blank=${webClientId.isNullOrBlank()})"
        )
        errorMessage = strings.authErrorGeneric
        return@rememberLauncherForActivityResult
      }
      finishGoogleSignIn(idToken)
    } catch (e: ApiException) {
      when (e.statusCode) {
        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
          Log.d(TAG_GOOGLE_AUTH, "Google Sign-In cancelled")
        }
        GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> {
          Log.d(TAG_GOOGLE_AUTH, "Google Sign-In already in progress")
        }
        else -> {
          Log.e(
            TAG_GOOGLE_AUTH,
            "Google Sign-In ApiException status=${e.statusCode} " +
              "(${GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)})",
            e
          )
          errorMessage = strings.authErrorGeneric
        }
      }
    } catch (e: Exception) {
      Log.e(TAG_GOOGLE_AUTH, "Google Sign-In unexpected error", e)
      errorMessage = strings.authErrorGeneric
    }
  }

  fun signInWithGoogle() {
    if (!showGoogleButton) return
    errorMessage = null
    val activity = context as? Activity
    if (activity == null) {
      errorMessage = strings.authErrorGeneric
      return
    }
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(webClientId)
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(activity, gso)
    // Clear cached Google session so account picker always returns a fresh idToken.
    scope.launch {
      isLoading = true
      try {
        client.signOut().await()
      } catch (e: Exception) {
        Log.w(TAG_GOOGLE_AUTH, "Google signOut before sign-in failed (continuing)", e)
      } finally {
        isLoading = false
      }
      googleSignInLauncher.launch(client.signInIntent)
    }
  }

  fun validateAndSubmit() {
    errorMessage = null
    infoMessage = null
    showResendVerification = false
    when {
      email.isBlank() -> errorMessage = strings.authErrorInvalidEmail
      !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
        errorMessage = strings.authErrorInvalidEmail
      password.length < 6 -> errorMessage = strings.authErrorWeakPassword
      isRegisterMode && password != passwordConfirm ->
        errorMessage = strings.authErrorPasswordMismatch
      else -> {
        scope.launch {
          isLoading = true
          try {
            if (isRegisterMode) {
              signInWithEmailPassword(email, password, register = true)
              infoMessage = strings.authVerifyEmailSent
              showResendVerification = true
              isRegisterMode = false
              passwordConfirm = ""
            } else {
              signInWithEmailPassword(email, password, register = false)
              finishIfAllowed()
            }
          } catch (e: Exception) {
            errorMessage = mapAuthError(e, strings)
          } finally {
            isLoading = false
          }
        }
      }
    }
  }

  fun sendPasswordReset() {
    errorMessage = null
    infoMessage = null
    when {
      email.isBlank() -> errorMessage = strings.authErrorInvalidEmail
      !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
        errorMessage = strings.authErrorInvalidEmail
      else -> {
        scope.launch {
          isLoading = true
          try {
            auth.sendPasswordResetEmail(email.trim()).await()
            infoMessage = strings.authResetEmailSent
            credentialStore.clear()
            rememberPassword = false
          } catch (e: Exception) {
            errorMessage = mapAuthError(e, strings)
          } finally {
            isLoading = false
          }
        }
      }
    }
  }

  fun resendVerificationEmail() {
    errorMessage = null
    infoMessage = null
    when {
      email.isBlank() -> errorMessage = strings.authErrorInvalidEmail
      !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
        errorMessage = strings.authErrorInvalidEmail
      password.length < 6 -> errorMessage = strings.authErrorWeakPassword
      else -> {
        scope.launch {
          isLoading = true
          try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = auth.currentUser
            if (user == null) {
              errorMessage = strings.authErrorGeneric
            } else {
              user.reload().await()
              if (user.isAllowedIntoApp()) {
                finishIfAllowed()
              } else {
                user.sendEmailVerification().await()
                auth.signOut()
                infoMessage = strings.authVerifyEmailSent
                showResendVerification = true
              }
            }
          } catch (e: Exception) {
            errorMessage = mapAuthError(e, strings)
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
        onValueChange = { email = it; errorMessage = null; infoMessage = null },
        label = { Text(strings.authEmail) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors
      )
      Spacer(modifier = Modifier.height(16.dp))
      OutlinedTextField(
        value = password,
        onValueChange = { password = it; errorMessage = null; infoMessage = null },
        label = { Text(strings.authPassword) },
        singleLine = true,
        visualTransformation = if (passwordVisible) {
          VisualTransformation.None
        } else {
          PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
          PasswordVisibilityToggle(
            visible = passwordVisible,
            onToggle = { passwordVisible = !passwordVisible }
          )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors
      )
      if (isRegisterMode) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
          value = passwordConfirm,
          onValueChange = { passwordConfirm = it; errorMessage = null; infoMessage = null },
          label = { Text(strings.authPasswordConfirm) },
          singleLine = true,
          visualTransformation = if (passwordConfirmVisible) {
            VisualTransformation.None
          } else {
            PasswordVisualTransformation()
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          trailingIcon = {
            PasswordVisibilityToggle(
              visible = passwordConfirmVisible,
              onToggle = { passwordConfirmVisible = !passwordConfirmVisible }
            )
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = fieldColors
        )
      } else {
        TextButton(
          onClick = { sendPasswordReset() },
          modifier = Modifier.align(Alignment.End),
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
          Text(
            text = strings.authForgotPassword,
            color = colors.primaryGlow,
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            rememberPassword = !rememberPassword
            if (!rememberPassword) credentialStore.clear()
          },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = rememberPassword,
          onCheckedChange = { checked ->
            rememberPassword = checked
            if (!checked) credentialStore.clear()
          },
          colors = CheckboxDefaults.colors(
            checkedColor = colors.primaryGlow,
            uncheckedColor = colors.textSecondary,
            checkmarkColor = colors.buttonText
          )
        )
        Text(
          text = strings.authRememberPassword,
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textPrimary
        )
      }
      errorMessage?.let { msg ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = msg,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error
        )
      }
      infoMessage?.let { msg ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = msg,
          style = MaterialTheme.typography.bodySmall,
          color = colors.primaryGlow
        )
      }
      if (showResendVerification && !isRegisterMode) {
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(
          onClick = { resendVerificationEmail() },
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
          Text(
            text = strings.authResendVerification,
            color = colors.primaryGlow,
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      ThemedFilledButton(
        text = if (isRegisterMode) strings.authRegister else strings.authLogin,
        onClick = { validateAndSubmit() },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(12.dp))
      TextButton(
        onClick = {
          isRegisterMode = !isRegisterMode
          passwordConfirm = ""
          errorMessage = null
          infoMessage = null
          showResendVerification = false
        }
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
private fun PasswordVisibilityToggle(
  visible: Boolean,
  onToggle: () -> Unit
) {
  val colors = LocalVoxeraTheme.current.colors
  IconButton(onClick = onToggle) {
    Icon(
      imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
      contentDescription = null,
      tint = colors.textSecondary
    )
  }
}

private fun mapAuthError(e: Exception, strings: Strings): String {
  val code = (e as? FirebaseAuthException)?.errorCode.orEmpty()
  val message = e.message.orEmpty().lowercase()
  return when {
    code in setOf("ERROR_EMAIL_ALREADY_IN_USE", "email-already-in-use") ||
      message.contains("email address is already in use") ||
      message.contains("already in use") -> strings.authErrorEmailInUse
    code in setOf("ERROR_INVALID_EMAIL", "invalid-email") -> strings.authErrorInvalidEmail
    code in setOf("ERROR_WRONG_PASSWORD", "wrong-password") -> strings.authErrorWrongPassword
    code in setOf("ERROR_USER_NOT_FOUND", "user-not-found") -> strings.authErrorUserNotFound
    code in setOf("ERROR_WEAK_PASSWORD", "weak-password") -> strings.authErrorWeakPassword
    else -> strings.authErrorGeneric
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
