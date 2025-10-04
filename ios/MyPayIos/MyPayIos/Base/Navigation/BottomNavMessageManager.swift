//
//  BottomNavMessageManager.swift
//  MyPayIos
//
//  Created by Claude Code on 19.09.25.
//

import Foundation
import Combine
import SwiftUI

// MARK: - Bottom Navigation Message Manager
@MainActor
final class BottomNavMessageManager: ObservableObject {
    
    // MARK: - Shared Instance
    static let shared = BottomNavMessageManager()
    
    // MARK: - Published Properties
    @Published private(set) var showMessage: Bool = false
    @Published private(set) var currentMessage: String = ""
    @Published private(set) var shouldDelayScroll: Bool = false
    
    // MARK: - Private Properties
    private var messageTask: Task<Void, Never>?
    private let messageDuration: TimeInterval = 3.0
    
    // MARK: - Initialization
    private init() {}
    
    // MARK: - Public Methods
    func showMessage(_ message: String) {
        // Cancel any existing message task
        messageTask?.cancel()
        
        // Start new message display sequence
        messageTask = Task {
            await displayMessage(message)
        }
    }
    
    func hideMessage() {
        messageTask?.cancel()
        withAnimation(.easeInOut(duration: 0.3)) {
            showMessage = false
            currentMessage = ""
            shouldDelayScroll = false
        }
    }
    
    // MARK: - Private Methods
    private func displayMessage(_ message: String) async {
        // Set delay scroll to prevent bottom nav animations during message display
        shouldDelayScroll = true
        
        // Set the message content
        currentMessage = message
        
        // Show message with animation
        withAnimation(.easeInOut(duration: 0.3)) {
            showMessage = true
        }
        
        // Wait for message duration
        do {
            try await Task.sleep(nanoseconds: UInt64(messageDuration * 1_000_000_000))
        } catch {
            // Task was cancelled
            return
        }
        
        // Hide message with animation
        withAnimation(.easeInOut(duration: 0.3)) {
            showMessage = false
        }
        
        // Wait for hide animation to complete
        do {
            try await Task.sleep(nanoseconds: 300_000_000) // 0.3 seconds
        } catch {
            return
        }
        
        // Reset state
        currentMessage = ""
        shouldDelayScroll = false
    }
}

// MARK: - Environment Key
struct BottomNavMessageManagerKey: EnvironmentKey {
    static let defaultValue = BottomNavMessageManager.shared
}

extension EnvironmentValues {
    var bottomNavMessageManager: BottomNavMessageManager {
        get { self[BottomNavMessageManagerKey.self] }
        set { self[BottomNavMessageManagerKey.self] = newValue }
    }
}