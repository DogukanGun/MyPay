import Foundation
import SwiftUI

// MARK: - App Constants
struct AppConstants {
    
    // MARK: - Animation
    struct Animation {
        static let defaultDuration: Double = 0.3
        static let splashDuration: TimeInterval = 3.0
        static let scrollDetectionDelay: TimeInterval = 0.1
    }
    
    // MARK: - UI
    struct UI {
        static let cornerRadius: CGFloat = 12
        static let buttonCornerRadius: CGFloat = 25
        static let padding: CGFloat = 16
        static let largePadding: CGFloat = 24
        static let bottomNavHeight: CGFloat = 80
        static let bottomNavCollapsedHeight: CGFloat = 50
        static let bottomNavCollapsedWidth: CGFloat = 80
        static let logoSize: CGSize = CGSize(width: 350, height: 300)
    }
    
    // MARK: - Colors
    struct Colors {
        static let primary = Color(red: 0.8, green: 1.0, blue: 0.2) // Lime green
        static let secondary = Color(red: 0.6, green: 0.4, blue: 1.0) // Purple
        static let background = Color(red: 0.95, green: 0.95, blue: 0.95)
        static let cardBackground = Color.white
        static let textPrimary = Color.black
        static let textSecondary = Color.gray
    }
    
    // MARK: - Strings
    struct Strings {
        static let appName = "MyPay"
        static let welcomeTitle = "Welcome to MyPay"
        static let loginSubtitle = "Connect your X account to continue"
        static let loginButtonText = "Login with"
        static let availableBalance = "Available Balance"
        static let portfolio = "Portfolio"
        static let accountInformation = "Account Information"
        static let pay = "Pay"
        static let receive = "Receive"
        static let name = "Name:"
        static let wallet = "Wallet:"
        static let verifier = "Verifier:"
    }
    
    // MARK: - Network
    struct Network {
        static let requestTimeout: TimeInterval = 30.0
    }
    
    // MARK: - Deep Links
    struct DeepLinks {
        static let scheme = "mypayios"
        static let oauthHost = "oauth"
    }
} 
