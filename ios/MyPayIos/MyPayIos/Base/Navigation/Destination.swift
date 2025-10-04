//
//  Destination.swift
//  MyPayIos
//
//  Created by Claude Code on 15.09.25.
//

import Foundation

// MARK: - Navigation Destinations
enum Destination: Hashable, CaseIterable {
    case splash
    case login
    case home
    case jarvis
    case settings
    
    // MARK: - Computed Properties
    static var navWithoutBottomBar: [Destination] {
        [.splash, .login]
    }
    
    var id: String {
        switch self {
        case .splash: return "splash"
        case .login: return "login"
        case .home: return "home"
        case .jarvis: return "jarvis"
        case .settings: return "settings"
        }
    }
    
    var title: String {
        switch self {
        case .splash: return "Splash"
        case .login: return "Login"
        case .home: return "Home"
        case .jarvis: return "Jarvis"
        case .settings: return "Settings"
        }
    }
}