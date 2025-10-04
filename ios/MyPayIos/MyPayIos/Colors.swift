//
//  Colors.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 11.09.25.
//

import SwiftUI

// MARK: - App Colors
extension Color {
    // Primary Colors
    static let primaryPurple = Color(red: 0.5, green: 0.3, blue: 0.8)
    static let primaryBlue = Color(red: 0.2, green: 0.4, blue: 0.9)
    static let primaryTeal = Color(red: 0.1, green: 0.7, blue: 0.8)
    
    // Background Colors
    static let backgroundDark = Color(red: 0.05, green: 0.05, blue: 0.1)
    static let backgroundLight = Color(red: 0.95, green: 0.95, blue: 1.0)
}

// MARK: - Gradients
extension LinearGradient {
    static let iconGradient = LinearGradient(
        colors: [Color("gradientStart"), Color("gradientEnd")],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let backgroundGradient = LinearGradient(
        colors: [Color.backgroundDark, Color.primaryPurple.opacity(0.3)],
        startPoint: .top,
        endPoint: .bottom
    )
} 
