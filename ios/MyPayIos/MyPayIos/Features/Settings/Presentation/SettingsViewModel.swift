//
//  SettingsViewModel.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import Foundation
import MessageUI

final class SettingsViewModel: BaseViewModel {
    
    @Published private(set) var viewState: SettingsViewState = .showSettings
    @Published var showAlert = false
    @Published var alertTitle = ""
    @Published var alertMessage = ""
    @Published var showMailComposer = false
    
    private let walletManager = WalletManager.shared
    func showList() {
        viewState = .showSettings
    }
    
    func showErrorMessage() {
        showComingSoonAlert()
    }
    
    func executeSetting(_ setting: Settings) {
        switch setting {
        case .feedbackForm:
            handleFeedbackForm()
        case .legal:
            handleLegal()
        case .privateKey:
            handlePrivateKey()
        }
    }
    
    private func handleFeedbackForm() {
        alertTitle = "Feedback Form"
        alertMessage = "Would you like to send feedback via email?"
        showAlert = true
    }
    
    private func handleLegal() {
        alertTitle = "Legal"
        alertMessage = """
        This application is provided "as is" without warranty of any kind. 
        
        By using this app, you agree to:
        - Use the app at your own risk
        - Keep your private keys secure
        - Not hold the developers liable for any losses
        
        For full terms and conditions, please visit our website.
        """
        showAlert = true
    }
    
    private func handlePrivateKey() {
        walletManager.getPrivateKey(for: .solana) { [weak self] privateKey in
            DispatchQueue.main.async {
                if let privateKey = privateKey {
                    self?.viewState = .showPrivateKey(privateKey)
                } else {
                    self?.viewState = .showError("Failed to retrieve private key")
                }
            }
        }
    }
    
    func sendFeedbackEmail() {
        if MFMailComposeViewController.canSendMail() {
            showMailComposer = true
        } else {
            alertTitle = "Email Not Available"
            alertMessage = "Please configure an email account on your device to send feedback."
            showAlert = true
        }
    }
    
    private func showComingSoonAlert() {
        alertTitle = "Coming Soon"
        alertMessage = "This feature will be available in a future update."
        showAlert = true
    }
    
    func logout() {
        // Clear all user data
        UserStorage.shared.deleteAllUserInfo()
        TokenStorage.shared.deleteToken()
        
        // Navigate to login
        navigationCoordinator.reset(to: .login)
    }
}
