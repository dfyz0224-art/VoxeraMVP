import java.util.Properties

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.gms.google-services")
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
  if (secretsFile.exists()) load(secretsFile.inputStream())
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
  if (keystorePropertiesFile.exists()) load(keystorePropertiesFile.inputStream())
}

android {
  namespace = "com.vanoprojects.voxera"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.vanoprojects.voxera"
    minSdk = 33
    targetSdk = 36
    versionCode = 4
    versionName = "0.1.3"
    buildConfigField("String", "VOXERA_API_TOKEN", "\"${secrets.getProperty("VOXERA_API_TOKEN", "")}\"")
    buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${secrets.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\"")
  }

  signingConfigs {
    create("release") {
      if (keystorePropertiesFile.exists()) {
        keyAlias = keystoreProperties.getProperty("keyAlias")
        keyPassword = keystoreProperties.getProperty("keyPassword")
        storeFile = rootProject.file(keystoreProperties.getProperty("storeFile")!!)
        storePassword = keystoreProperties.getProperty("storePassword")
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      if (keystorePropertiesFile.exists()) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
  }

  buildFeatures { compose = true; buildConfig = true }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.activity:activity-compose:1.10.1")
  implementation("androidx.core:core-ktx:1.16.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")

  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")

  implementation("androidx.compose.material3:material3")
  implementation("androidx.navigation:navigation-compose:2.9.0")
  implementation("io.github.fletchmckee.liquid:liquid:1.1.1")
  implementation("androidx.datastore:datastore-preferences:1.1.1")

  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

  implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
  implementation("com.google.firebase:firebase-analytics")
  implementation("com.google.firebase:firebase-auth")
  implementation("com.google.android.gms:play-services-auth:21.3.0")
  implementation("io.coil-kt:coil-compose:2.5.0")
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
