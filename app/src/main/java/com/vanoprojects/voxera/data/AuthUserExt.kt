package com.vanoprojects.voxera.data

import com.google.firebase.auth.FirebaseUser

/** Email/password accounts must verify email before using the app. Google etc. are trusted. */
fun FirebaseUser.requiresEmailVerification(): Boolean {
  val hasPasswordProvider = providerData.any { it.providerId == "password" }
  return hasPasswordProvider && !isEmailVerified
}

fun FirebaseUser.isAllowedIntoApp(): Boolean = !requiresEmailVerification()
