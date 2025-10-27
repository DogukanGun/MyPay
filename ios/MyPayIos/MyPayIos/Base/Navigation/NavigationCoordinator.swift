//
//  NavigationCoordinator.swift
//  MyPayIos
//
//  Created by Claude Code on 15.09.25.
//

import Foundation
import SwiftUI
import Combine

// MARK: - Navigation Coordinator Protocol
protocol NavigationCoordinatorProtocol: ObservableObject {
    var navigationPath: NavigationPath { get }
    var currentDestination: Destination { get }
    
    func navigate(to destination: Destination)
    func navigateUp()
    func reset(to destination: Destination)
    func popToRoot()
    func handleDeepLink(_ url: URL)
}

// MARK: - Navigation Coordinator Implementation
final class NavigationCoordinator: NavigationCoordinatorProtocol {
    
    // MARK: - Shared Instance
    static let shared = NavigationCoordinator()
    
    // MARK: - Published Properties
    @Published var navigationPath = NavigationPath()
    @Published private(set) var currentDestination: Destination
    
    // MARK: - Private Properties
    private let startDestination: Destination
    private var cancellables = Set<AnyCancellable>()
    
    // MARK: - Initialization
    private init(startDestination: Destination = .splash) {
        self.startDestination = startDestination
        self.currentDestination = startDestination
        setupObservers()
    }
    
    deinit {
        cancellables.removeAll()
    }
    
    // MARK: - Navigation Methods
    func navigate(to destination: Destination) {
        guard destination != currentDestination else { return }
        
        withAnimation(.easeInOut(duration: AppConstants.Animation.defaultDuration)) {
            navigationPath.append(destination)
            updateCurrentDestination(destination)
        }
    }
    
    func navigateUp() {
        guard !navigationPath.isEmpty else { return }
        
        withAnimation(.easeInOut(duration: AppConstants.Animation.defaultDuration)) {
            navigationPath.removeLast()
            // Update current destination based on remaining path
            updateCurrentDestinationFromPath()
        }
    }
    
    func reset(to destination: Destination) {
        withAnimation(.easeInOut(duration: AppConstants.Animation.defaultDuration)) {
            navigationPath.removeLast(navigationPath.count)
            currentDestination = destination
        }
    }
    
    func popToRoot() {
        withAnimation(.easeInOut(duration: AppConstants.Animation.defaultDuration)) {
            navigationPath.removeLast(navigationPath.count)
            currentDestination = startDestination
        }
    }
    
    // MARK: - Deep Link Support
    func handleDeepLink(_ url: URL) {
        guard let destination = parseDeepLink(url) else {
            print("Unable to parse deep link: \(url)")
            return
        }
        
        print("Handling deep link to: \(destination)")
        reset(to: destination)
    }
    
    // MARK: - Private Methods
    private func setupObservers() {
        $navigationPath
            .sink { [weak self] _ in
                self?.updateCurrentDestinationFromPath()
            }
            .store(in: &cancellables)
    }
    
    private func updateCurrentDestination(_ destination: Destination) {
        guard destination != currentDestination else { return }
        currentDestination = destination
    }
    
    private func updateCurrentDestinationFromPath() {
        // This is a simplified implementation
        // In a more complex app, you might need to track the actual path content
        if navigationPath.isEmpty {
            currentDestination = startDestination
        }
    }
    
    private func parseDeepLink(_ url: URL) -> Destination? {
        // Validate scheme
        guard url.scheme == AppConstants.DeepLinks.scheme else {
            return nil
        }
        
        let pathComponents = url.pathComponents.filter { $0 != "/" }
        
        // Map path components to destinations
        if pathComponents.contains("home") {
            return .home
        } else if pathComponents.contains("login") {
            return .login
        } else if pathComponents.contains("jarvis") {
            return .jarvis
        } else if pathComponents.contains("settings") {
            return .settings
        }
        
        return nil
    }
}
