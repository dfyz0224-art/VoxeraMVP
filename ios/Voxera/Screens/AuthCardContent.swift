import SwiftUI

/// Shared login/register form (Android AuthCardContent parity).
struct AuthCardContent: View {
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @EnvironmentObject private var history: HistoryStore

  var showSkipButton: Bool = true
  var onAuthComplete: () -> Void
  var onSkip: (() -> Void)? = nil

  @State private var email = ""
  @State private var password = ""
  @State private var passwordConfirm = ""
  @State private var passwordVisible = false
  @State private var passwordConfirmVisible = false
  @State private var rememberPassword = false
  @State private var isRegisterMode = false
  @State private var isLoading = false
  @State private var errorMessage: String?
  @State private var infoMessage: String?
  @State private var showResendVerification = false
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
          .onChange(of: email) { _, _ in clearMessages() }

        passwordField(s.authPassword, text: $password, visible: $passwordVisible)

        if isRegisterMode {
          passwordField(s.authPasswordConfirm, text: $passwordConfirm, visible: $passwordConfirmVisible)
        }

        if !isRegisterMode {
          Button(s.authForgotPassword) { sendPasswordReset() }
            .font(.footnote)
            .foregroundColor(.white.opacity(0.9))
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
        if let infoMessage {
          Text(infoMessage)
            .font(.footnote)
            .foregroundColor(.white.opacity(0.92))
        }

        if showResendVerification && !isRegisterMode {
          Button(s.authResendVerification) { resendVerification() }
            .font(.footnote)
            .foregroundColor(.white.opacity(0.95))
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
          clearMessages()
          showResendVerification = false
        }
        .font(.footnote)
        .foregroundColor(.white.opacity(0.85))

        if showSkipButton {
          Button(s.authSkip) {
            history.setAccountKey(HistoryStore.guestAccountKey)
            if let onSkip {
              onSkip()
            } else {
              prefs.setAuthCompleted(true)
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

  private func passwordField(_ title: String, text: Binding<String>, visible: Binding<Bool>) -> some View {
    HStack {
      Group {
        if visible.wrappedValue {
          TextField(title, text: text)
        } else {
          SecureField(title, text: text)
        }
      }
      .textInputAutocapitalization(.never)
      .autocorrectionDisabled()
      .foregroundColor(.white)
      .onChange(of: text.wrappedValue) { _, _ in clearMessages() }

      Button {
        visible.wrappedValue.toggle()
      } label: {
        Image(systemName: visible.wrappedValue ? "eye.slash" : "eye")
          .foregroundColor(.white.opacity(0.85))
      }
      .buttonStyle(.plain)
    }
    .padding(12)
    .background(Color.white.opacity(0.12))
    .cornerRadius(12)
  }

  private func clearMessages() {
    errorMessage = nil
    infoMessage = nil
  }

  private func attemptAutoLogin() {
    guard !autoLoginAttempted, !isRegisterMode, rememberPassword else { return }
    guard let saved = CredentialStore.load(), saved.password.count >= 6 else { return }
    autoLoginAttempted = true
    isLoading = true
    clearMessages()
    Task {
      do {
        try await AuthBackend.signIn(email: saved.email, password: saved.password, register: false)
        await finishIfAllowed()
      } catch let err as AuthBackend.AuthError {
        await MainActor.run {
          if case .emailNotVerified = err {
            showResendVerification = true
            errorMessage = s.authEmailNotVerified
          }
          CredentialStore.clear()
          rememberPassword = false
          password = ""
          isLoading = false
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

  @MainActor
  private func finishIfAllowed() async {
    do {
      try await AuthBackend.ensureAllowedIntoApp()
      if rememberPassword {
        CredentialStore.save(email: email.trimmingCharacters(in: .whitespacesAndNewlines), password: password)
      } else {
        CredentialStore.clear()
      }
      history.setAccountKey(AuthBackend.accountKey())
      showResendVerification = false
      prefs.setAuthCompleted(true)
      isLoading = false
      onAuthComplete()
    } catch {
      showResendVerification = true
      AuthBackend.signOut()
      errorMessage = s.authEmailNotVerified
      isLoading = false
    }
  }

  private func validateAndSubmit() {
    clearMessages()
    showResendVerification = false
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
        if isRegisterMode {
          try await AuthBackend.signIn(email: trimmed, password: password, register: true)
          await MainActor.run {
            infoMessage = s.authVerifyEmailSent
            showResendVerification = true
            isRegisterMode = false
            passwordConfirm = ""
            isLoading = false
          }
        } else {
          try await AuthBackend.signIn(email: trimmed, password: password, register: false)
          await finishIfAllowed()
        }
      } catch let err as AuthBackend.AuthError {
        await MainActor.run {
          if case .emailNotVerified = err {
            showResendVerification = true
            errorMessage = s.authEmailNotVerified
          } else {
            errorMessage = mapError(err)
          }
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

  private func sendPasswordReset() {
    clearMessages()
    let trimmed = email.trimmingCharacters(in: .whitespacesAndNewlines)
    if trimmed.isEmpty || !trimmed.contains("@") {
      errorMessage = s.authErrorInvalidEmail
      return
    }
    isLoading = true
    Task {
      do {
        try await AuthBackend.sendPasswordReset(email: trimmed)
        await MainActor.run {
          infoMessage = s.authResetEmailSent
          CredentialStore.clear()
          rememberPassword = false
          isLoading = false
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

  private func resendVerification() {
    clearMessages()
    let trimmed = email.trimmingCharacters(in: .whitespacesAndNewlines)
    if trimmed.isEmpty || password.count < 6 {
      errorMessage = s.authErrorWeakPassword
      return
    }
    isLoading = true
    Task {
      do {
        try await AuthBackend.resendVerification(email: trimmed, password: password)
        await finishIfAllowed()
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
    case .emailNotVerified: return s.authEmailNotVerified
    case .generic: return s.authErrorGeneric
    }
  }
}

/// Auth backend: local session with email-verification parity (Android Firebase flow mirrored for UX).
enum AuthBackend {
  enum AuthError: Error {
    case invalidEmail, wrongPassword, userNotFound, emailInUse, weakPassword, emailNotVerified, generic
  }

  private static let accountsKey = "voxera_local_accounts_v2"
  private static let signedInKey = "voxera_signed_in_email"

  static var currentEmail: String? {
    UserDefaults.standard.string(forKey: signedInKey)
  }

  static var isSignedIn: Bool { currentEmail != nil }

  static func accountKey() -> String {
    currentEmail?.lowercased() ?? HistoryStore.guestAccountKey
  }

  static func signOut() {
    UserDefaults.standard.removeObject(forKey: signedInKey)
    CredentialStore.clear()
  }

  static func signIn(email: String, password: String, register: Bool) async throws {
    if password.count < 6 { throw AuthError.weakPassword }
    if !email.contains("@") { throw AuthError.invalidEmail }

    var accounts = loadAccounts()
    let key = email.lowercased()

    if register {
      if accounts[key] != nil { throw AuthError.emailInUse }
      accounts[key] = LocalAccount(password: password, verified: false)
      saveAccounts(accounts)
      // Mirror Android: do not keep session after register until email verified.
      UserDefaults.standard.removeObject(forKey: signedInKey)
      return
    }

    guard let stored = accounts[key] else { throw AuthError.userNotFound }
    if stored.password != password { throw AuthError.wrongPassword }
    if !stored.verified { throw AuthError.emailNotVerified }
    UserDefaults.standard.set(email, forKey: signedInKey)
  }

  static func ensureAllowedIntoApp() async throws {
    guard let email = currentEmail?.lowercased() else { throw AuthError.generic }
    let accounts = loadAccounts()
    guard let stored = accounts[email] else { throw AuthError.userNotFound }
    if !stored.verified { throw AuthError.emailNotVerified }
  }

  static func sendPasswordReset(email: String) async throws {
    if !email.contains("@") { throw AuthError.invalidEmail }
    var accounts = loadAccounts()
    let key = email.lowercased()
    guard accounts[key] != nil else { throw AuthError.userNotFound }
    // Local stub: mark as needing a new password by clearing remember-only; account stays.
    // UX message matches Android "reset email sent".
    _ = accounts
  }

  static func resendVerification(email: String, password: String) async throws {
    var accounts = loadAccounts()
    let key = email.lowercased()
    guard var stored = accounts[key] else { throw AuthError.userNotFound }
    if stored.password != password { throw AuthError.wrongPassword }
    // Local/dev: "resend" confirms the mailbox so the user can proceed (no SMTP on device).
    stored.verified = true
    accounts[key] = stored
    saveAccounts(accounts)
    UserDefaults.standard.set(email, forKey: signedInKey)
  }

  private struct LocalAccount: Codable {
    var password: String
    var verified: Bool
  }

  private static func loadAccounts() -> [String: LocalAccount] {
    let d = UserDefaults.standard
    if let data = d.data(forKey: accountsKey),
      let decoded = try? JSONDecoder().decode([String: LocalAccount].self, from: data)
    {
      return decoded
    }
    // Migrate legacy plain password map.
    if let legacy = d.dictionary(forKey: "voxera_local_accounts") as? [String: String] {
      var migrated: [String: LocalAccount] = [:]
      for (k, v) in legacy {
        migrated[k] = LocalAccount(password: v, verified: true)
      }
      saveAccounts(migrated)
      return migrated
    }
    return [:]
  }

  private static func saveAccounts(_ accounts: [String: LocalAccount]) {
    if let data = try? JSONEncoder().encode(accounts) {
      UserDefaults.standard.set(data, forKey: accountsKey)
    }
  }
}
