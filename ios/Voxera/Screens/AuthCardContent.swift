import SwiftUI

/// Shared login/register form (Android AuthCardContent parity).
struct AuthCardContent: View {
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore

  var showSkipButton: Bool = true
  var onAuthComplete: () -> Void
  var onSkip: (() -> Void)? = nil

  @State private var email = ""
  @State private var password = ""
  @State private var passwordConfirm = ""
  @State private var rememberPassword = false
  @State private var isRegisterMode = false
  @State private var isLoading = false
  @State private var errorMessage: String?
  @State private var autoLoginAttempted = false

  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      VStack(alignment: .leading, spacing: 14) {
        TextField(s.authEmail, text: $email)
          .textContentType(.emailAddress)
          .keyboardType(.emailAddress)
          .textInputAutocapitalization(.never)
          .autocorrectionDisabled()
          .padding(12)
          .background(Color.white.opacity(0.12))
          .cornerRadius(12)
          .foregroundColor(.white)
          .onChange(of: email) { _, _ in errorMessage = nil }

        SecureField(s.authPassword, text: $password)
          .padding(12)
          .background(Color.white.opacity(0.12))
          .cornerRadius(12)
          .foregroundColor(.white)
          .onChange(of: password) { _, _ in errorMessage = nil }

        if isRegisterMode {
          SecureField(s.authPasswordConfirm, text: $passwordConfirm)
            .padding(12)
            .background(Color.white.opacity(0.12))
            .cornerRadius(12)
            .foregroundColor(.white)
            .onChange(of: passwordConfirm) { _, _ in errorMessage = nil }
        }

        Button {
          rememberPassword.toggle()
          if !rememberPassword { CredentialStore.clear() }
        } label: {
          HStack(spacing: 10) {
            Image(systemName: rememberPassword ? "checkmark.square.fill" : "square")
              .foregroundColor(.white)
            Text(s.authRememberPassword)
              .foregroundColor(.white)
            Spacer()
          }
        }
        .buttonStyle(.plain)

        if let errorMessage {
          Text(errorMessage)
            .font(.footnote)
            .foregroundColor(.red.opacity(0.95))
        }

        Button(isRegisterMode ? s.authRegister : s.authLogin) {
          validateAndSubmit()
        }
        .buttonStyle(.borderedProminent)
        .tint(.white.opacity(0.35))
        .disabled(isLoading)
        .frame(maxWidth: .infinity)

        Button(isRegisterMode ? s.authLoginHint : s.authRegisterHint) {
          isRegisterMode.toggle()
          passwordConfirm = ""
          errorMessage = nil
        }
        .font(.footnote)
        .foregroundColor(.white.opacity(0.85))

        if showSkipButton {
          Button(s.authSkip) {
            if let onSkip {
              onSkip()
            } else {
              onAuthComplete()
            }
          }
          .foregroundColor(.white.opacity(0.9))
          .frame(maxWidth: .infinity)
        }
      }

      if isLoading {
        ProgressView()
          .tint(.white)
          .scaleEffect(1.2)
      }
    }
    .onAppear {
      rememberPassword = CredentialStore.isRememberEnabled
      if let saved = CredentialStore.load() {
        email = saved.email
        password = saved.password
      }
      attemptAutoLogin()
    }
  }

  private func attemptAutoLogin() {
    guard !autoLoginAttempted, !isRegisterMode, rememberPassword else { return }
    guard let saved = CredentialStore.load(), saved.password.count >= 6 else { return }
    autoLoginAttempted = true
    isLoading = true
    errorMessage = nil
    Task {
      do {
        try await AuthBackend.signIn(email: saved.email, password: saved.password, register: false)
        await MainActor.run {
          prefs.setAuthCompleted(true)
          isLoading = false
          onAuthComplete()
        }
      } catch {
        await MainActor.run {
          CredentialStore.clear()
          rememberPassword = false
          password = ""
          isLoading = false
        }
      }
    }
  }

  private func validateAndSubmit() {
    errorMessage = nil
    let trimmed = email.trimmingCharacters(in: .whitespacesAndNewlines)
    if trimmed.isEmpty || !trimmed.contains("@") || !trimmed.contains(".") {
      errorMessage = s.authErrorInvalidEmail
      return
    }
    if password.count < 6 {
      errorMessage = s.authErrorWeakPassword
      return
    }
    if isRegisterMode && password != passwordConfirm {
      errorMessage = s.authErrorPasswordMismatch
      return
    }
    isLoading = true
    Task {
      do {
        try await AuthBackend.signIn(email: trimmed, password: password, register: isRegisterMode)
        await MainActor.run {
          if rememberPassword {
            CredentialStore.save(email: trimmed, password: password)
          } else {
            CredentialStore.clear()
          }
          prefs.setAuthCompleted(true)
          isLoading = false
          onAuthComplete()
        }
      } catch let err as AuthBackend.AuthError {
        await MainActor.run {
          errorMessage = mapError(err)
          isLoading = false
        }
      } catch {
        await MainActor.run {
          errorMessage = s.authErrorGeneric
          isLoading = false
        }
      }
    }
  }

  private func mapError(_ err: AuthBackend.AuthError) -> String {
    switch err {
    case .invalidEmail: return s.authErrorInvalidEmail
    case .wrongPassword: return s.authErrorWrongPassword
    case .userNotFound: return s.authErrorUserNotFound
    case .emailInUse: return s.authErrorEmailInUse
    case .weakPassword: return s.authErrorWeakPassword
    case .generic: return s.authErrorGeneric
    }
  }
}

/// Auth backend: uses Firebase when configured, otherwise local session (dev/parity without plist).
enum AuthBackend {
  enum AuthError: Error {
    case invalidEmail, wrongPassword, userNotFound, emailInUse, weakPassword, generic
  }

  static var currentEmail: String? {
    UserDefaults.standard.string(forKey: "voxera_signed_in_email")
  }

  static var isSignedIn: Bool {
    currentEmail != nil
  }

  static func signOut() {
    UserDefaults.standard.removeObject(forKey: "voxera_signed_in_email")
    CredentialStore.clear()
  }

  static func signIn(email: String, password: String, register: Bool) async throws {
    // Local parity backend (same UX gates as Android). Replace with Firebase Auth when GoogleService-Info is present.
    if password.count < 6 { throw AuthError.weakPassword }
    if !email.contains("@") { throw AuthError.invalidEmail }

    let accountsKey = "voxera_local_accounts"
    var accounts = UserDefaults.standard.dictionary(forKey: accountsKey) as? [String: String] ?? [:]
    let key = email.lowercased()

    if register {
      if accounts[key] != nil { throw AuthError.emailInUse }
      accounts[key] = password
      UserDefaults.standard.set(accounts, forKey: accountsKey)
    } else {
      guard let stored = accounts[key] else { throw AuthError.userNotFound }
      if stored != password { throw AuthError.wrongPassword }
    }
    UserDefaults.standard.set(email, forKey: "voxera_signed_in_email")
  }
}
