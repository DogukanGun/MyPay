//
//  JarvisViewState.swift
//  MyPayIos
//
//  Created by AI Assistant on 16.10.25.
//

import Foundation

// MARK: - Jarvis View State
enum JarvisViewState: Equatable {
    case idle
    case askPermission
    case listening
    case confirming(transcription: String)
    case processing
    case success(message: String)
    case error(message: String)
    
    var isActive: Bool {
        switch self {
        case .listening, .processing:
            return true
        default:
            return false
        }
    }
    
    var displayText: String {
        switch self {
        case .idle:
            return "Tap the circle to speak with Jarvis"
        case .askPermission:
            return "Microphone permission needed"
        case .listening:
            return "Listening..."
        case .confirming:
            return "Review your message"
        case .processing:
            return "Processing..."
        case .success:
            return "Success"
        case .error(let message):
            return "Error: \(message)"
        }
    }
}

