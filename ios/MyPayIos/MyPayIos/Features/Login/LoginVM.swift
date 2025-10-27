//
//  LoginViewModel.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 16.09.25.
//

import Foundation
import FirebaseAuth
import FirebaseCore

// MARK: - Login View Model
final class LoginViewModel: BaseViewModel {
    
    // MARK: - Private Properties
    private let provider = OAuthProvider(providerID: "twitter.com")
    
    // MARK: - Initialization
    override init() {
        super.init()
        setupProvider()
    }
    
    // MARK: - Private Methods
    private func setupProvider() {
        provider.scopes = ["tweet.read", "users.read", "offline.access"]
        provider.customParameters = ["lang": "en"]
    }
    
    // MARK: - Public Methods
    func startAuthFlow() {
        executeAsync(
            operation: { [weak self] in
                try await self?.performAuthentication()
            },
            onSuccess: { [weak self] _ in
                DispatchQueue.main.async {
                    self?.navigationCoordinator.reset(to: .home)
                    
                    // Show welcome message after navigation
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        BottomNavMessageManager.shared.showMessage("Welcome!")
                    }
                }
            }
        )
    }
    
    // MARK: - Private Authentication Methods
    private func performAuthentication() async throws {
        guard FirebaseApp.app() != nil else {
            throw AppError.authentication(.firebaseNotInitialized)
        }
                
        return try await withCheckedThrowingContinuation { continuation in
            provider.getCredentialWith(nil) { [weak self] credential, error in
                if let error = error {
                    let authError = self?.mapFirebaseError(error) ?? .authentication(.unknown(error.localizedDescription))
                    continuation.resume(throwing: authError)
                    return
                }
                
                guard let credential = credential else {
                    continuation.resume(throwing: AppError.authentication(.invalidCredentials))
                    return
                }
                
                print("Got credential, signing in with Firebase...")
                
                Auth.auth().signIn(with: credential) { authResult, error in
                    if let error = error {
                        let authError = self?.mapFirebaseError(error) ?? .authentication(.unknown(error.localizedDescription))
                        continuation.resume(throwing: authError)
                        return
                    }
                    
                    guard let authResult = authResult else {
                        continuation.resume(throwing: AppError.authentication(.invalidCredentials))
                        return
                    }
                    print("Sign in successful!")
                    print("User ID: \(authResult.user.uid)")
                    UserStorage.shared.saveTwitterId(authResult.user.uid)
                    UserStorage.shared.saveDisplayName(authResult.user.displayName)
                    
                    authResult.user.getIDToken { [weak self] idToken, error in
                        if let error = error {
                            print("Error getting ID token: \(error)")
                            continuation.resume(throwing: AppError.authentication(.unknown(error.localizedDescription)))
                            return
                        }
                        
                        if let idToken = idToken {
                            print("ID Token: \(idToken)")
                            // Save token to secure storage
                            TokenStorage.shared.saveToken(idToken)
                            
                            // Check if user needs registration and handle wallet setup
                            self?.handleUserRegistration(
                                authResult: authResult,
                                continuation: continuation
                            )
                        } else {
                            print("ID Token is nil")
                            continuation.resume(throwing: AppError.authentication(.invalidCredentials))
                        }
                    }
                }
            }
        }
    }
    
    private func mapFirebaseError(_ error: Error) -> AppError {
        guard let authError = error as NSError? else {
            return .authentication(.unknown(error.localizedDescription))
        }
        
        print("Firebase Auth error - Code: \(authError.code), Domain: \(authError.domain)")
        
        if authError.domain == "FIRAuthErrorDomain" {
            switch authError.code {
            case 17999: // FIRAuthErrorCodeWebSignInUserInteractionFailure
                print("User cancelled or web sign-in failed")
                return .authentication(.userCancelled)
            case 17020: // FIRAuthErrorCodeNetworkError
                print("Network error during sign-in")
                return .authentication(.networkError)
            case 17068: // FIRAuthErrorCodeWebContextCancelled
                print("Web context was cancelled")
                return .authentication(.userCancelled)
            case 17046: // FIRAuthErrorCodeWebInternalError
                print("Internal web error - check Twitter app configuration")
                return .authentication(.configurationError)
            default:
                print("Firebase Auth error code: \(authError.code)")
                return .authentication(.unknown(authError.localizedDescription))
            }
        }
        
        return .authentication(.unknown(authError.localizedDescription))
    }
    
    private func handleUserRegistration(
        authResult: AuthDataResult,
        continuation: CheckedContinuation<Void, Error>
    ) {
        let authApi = AuthApi.shared
        
        // First check if user exists
        authApi.checkIfUserExists { [weak self] result in
            switch result {
            case .success(let response):
                if response.register {
                    // User needs to register, call register API
                    self?.registerUser(authResult: authResult, continuation: continuation)
                } else if response.device_changed {
                    self?.handleDeviceChange(continuation: continuation)
                } else {
                    // User already exists, proceed normally
                    continuation.resume()
                }
            case .failure(let error):
                print("Failed to check user existence: \(error)")
                continuation.resume(throwing: error)
            }
        }
    }
    
    private func handleDeviceChange(
        continuation: CheckedContinuation<Void, Error>
    ) {
        AuthApi.shared.newSession { result in
            switch result {
            case .success(let newSessionResponse):
                WalletManager.shared.storeWallets(ethWallet: newSessionResponse.wallets?.ethWallet, solanaWallet: newSessionResponse.wallets?.solanaWallet) { saveWalletInfoStatus in
                    if saveWalletInfoStatus {
                        continuation.resume()
                    } else {
                        continuation.resume(throwing: AppError.unknown("Wallet cannot be saved"))
                    }
                }
            case .failure(let error):
                print("Failed to register user: \(error)")
                continuation.resume(throwing: error)
            }
        }
    }
    
    private func registerUser(
        authResult: AuthDataResult,
        continuation: CheckedContinuation<Void, Error>
    ) {
        guard let twitterId = authResult.user.providerData.first?.uid,
              let username = authResult.user.displayName else {
            continuation.resume(throwing: AppError.authentication(.invalidCredentials))
            return
        }
        
        let authApi = AuthApi.shared
        authApi.registerUser(twitterId: twitterId, username: username) { result in
            switch result {
            case .success(let registerResponse):
                print("User registered successfully!")
                
                // Store wallets securely
                WalletManager.shared.storeWallets(
                    ethWallet: registerResponse.ethWallet,
                    solanaWallet: registerResponse.solanaWallet
                ) { success in
                    if success {
                        print("Wallets stored securely")
                        continuation.resume()
                    } else {
                        print("Failed to store wallets securely")
                        continuation.resume(throwing: AppError.authentication(.unknown("Failed to secure wallets")))
                    }
                }
                
            case .failure(let error):
                print("Failed to register user: \(error)")
                continuation.resume(throwing: error)
            }
        }
    }
}
