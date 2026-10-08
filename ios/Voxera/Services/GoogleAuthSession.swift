import Foundation
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
}
