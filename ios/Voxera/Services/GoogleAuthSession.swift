import Foundation
import AuthenticationServices
import CryptoKit
import FirebaseCore
import FirebaseAuth
import GoogleSignIn
import UIKit

enum FirebaseBootstrap {
  static func configureIfPossible() {
    guard FirebaseApp.app() == nil else { return }
    guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return }
    FirebaseApp.configure()
  }

  static var isReady: Bool { FirebaseApp.app() != nil }
}

@MainActor
final class GoogleAuthSession: ObservableObject {
  @Published private(set) var userId: String?
  @Published var message: String?

  private var listener: AuthStateDidChangeListenerHandle?
  private var currentNonce: String?

  init() {
    FirebaseBootstrap.configureIfPossible()
    guard FirebaseBootstrap.isReady else { return }
    userId = Auth.auth().currentUser?.uid
    listener = Auth.auth().addStateDidChangeListener { _, user in
      Task { @MainActor in
        self.userId = user?.uid
        if let uid = user?.uid {
          TrialAccess.startIfNeeded(uid: uid)
        }
      }
    }
  }

  var isSignedIn: Bool { userId != nil }

  var email: String? {
    guard FirebaseBootstrap.isReady else { return nil }
    return Auth.auth().currentUser?.email ?? Auth.auth().currentUser?.displayName
  }

  func signIn(presenting: UIViewController) {
    FirebaseBootstrap.configureIfPossible()
    guard FirebaseBootstrap.isReady, let clientID = FirebaseApp.app()?.options.clientID else {
      message = "Add GoogleService-Info.plist for bundle com.vanoprojects.voxera.app and set GOOGLE_REVERSED_CLIENT_ID."
      return
    }
    GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
    GIDSignIn.sharedInstance.signIn(withPresenting: presenting) { result, error in
      if let error {
        Task { @MainActor in self.message = error.localizedDescription }
        return
      }
      guard let user = result?.user, let idToken = user.idToken?.tokenString else {
        Task { @MainActor in self.message = "Google sign-in did not return a token." }
        return
      }
      let credential = GoogleAuthProvider.credential(
        withIDToken: idToken,
        accessToken: user.accessToken.tokenString
      )
      Auth.auth().signIn(with: credential) { _, error in
        Task { @MainActor in
          if let error {
            self.message = error.localizedDescription
          } else if let uid = Auth.auth().currentUser?.uid {
            TrialAccess.startIfNeeded(uid: uid)
            self.message = nil
          }
        }
      }
    }
  }

  func signOut() {
    GIDSignIn.sharedInstance.signOut()
    try? Auth.auth().signOut()
    userId = nil
  }

  func prepareAppleRequest(_ request: ASAuthorizationAppleIDRequest) {
    let nonce = Self.randomNonce()
    currentNonce = nonce
    request.requestedScopes = [.fullName, .email]
    request.nonce = Self.sha256(nonce)
  }

  func completeApple(_ result: Result<ASAuthorization, Error>) {
    switch result {
    case .failure(let error):
      if (error as NSError).code == ASAuthorizationError.canceled.rawValue { return }
      message = error.localizedDescription
    case .success(let authorization):
      guard let apple = authorization.credential as? ASAuthorizationAppleIDCredential,
            let tokenData = apple.identityToken,
            let token = String(data: tokenData, encoding: .utf8),
            let nonce = currentNonce else {
        message = "Apple sign-in did not return a token."
        return
      }
      FirebaseBootstrap.configureIfPossible()
      guard FirebaseBootstrap.isReady else {
        message = "Add GoogleService-Info.plist before signing in with Apple."
        return
      }
      let credential = OAuthProvider.appleCredential(
        withIDToken: token,
        rawNonce: nonce,
        fullName: apple.fullName
      )
      Auth.auth().signIn(with: credential) { _, error in
        Task { @MainActor in
          if let error {
            self.message = error.localizedDescription
          } else if let uid = Auth.auth().currentUser?.uid {
            TrialAccess.startIfNeeded(uid: uid)
            self.message = nil
          }
        }
      }
    }
  }

  private static func randomNonce(length: Int = 32) -> String {
    let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
    var bytes = [UInt8](repeating: 0, count: length)
    let status = SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes)
    if status != errSecSuccess {
      return UUID().uuidString.replacingOccurrences(of: "-", with: "")
    }
    return String(bytes.map { charset[Int($0) % charset.count] })
  }

  private static func sha256(_ input: String) -> String {
    let hashed = SHA256.hash(data: Data(input.utf8))
    return hashed.map { String(format: "%02x", $0) }.joined()
  }
}
