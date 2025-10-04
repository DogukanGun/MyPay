//
//  SplashViewModel.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 11.09.25.
//

import Foundation
import SwiftUI
import Combine
import UIKit

// MARK: - Splash View Model
final class SplashViewModel: BaseViewModel {
    @Published private(set) var viewState: SplashVS?
    
    // MARK: - Initialization
    override init() {
        super.init()
        startApp()
    }
    
    // MARK: - Public Methods
    func closeApp() {
        exit(0)
    }
    
    // MARK: - Private Methods
    private func startApp() {
        setLoading(true)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + AppConstants.Animation.splashDuration) { [weak self] in
            self?.setLoading(false)
            
            // Check if user is authenticated (simplified for now)
            let isAuthenticated = self?.checkUserAuthentication() ?? false
            let destination: Destination = isAuthenticated ? .home : .login
            self?.viewState = .startApp(destination: destination)
        }
    }
    
    private func checkUserAuthentication() -> Bool {
        // TODO: Implement actual authentication check
        // This could check for stored tokens, Firebase auth state, etc.
        return false
    }
}

