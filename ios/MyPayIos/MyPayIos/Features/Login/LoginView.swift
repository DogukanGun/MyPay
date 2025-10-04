//
//  LoginView.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 11.09.25.
//

import SwiftUI

// MARK: - Login View
struct LoginView: View {
    @StateObject private var viewModel = LoginViewModel()
    
    init() {
        // ViewModel now uses NavigationCoordinator.shared by default
    }
    
    var body: some View {
        ZStack {
            LinearGradient.iconGradient
                .ignoresSafeArea(.all)
            
            VStack(spacing: 30) {
                Spacer()
                // Title
                Text(AppConstants.Strings.welcomeTitle)
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(AppConstants.Colors.textPrimary)
                
                // Subtitle
                Text(AppConstants.Strings.loginSubtitle)
                    .font(.title2)
                    .foregroundColor(AppConstants.Colors.textPrimary.opacity(0.8))
                    .padding(.bottom, 64)
                
                Spacer()
                
                // Login Button
                LoginButton(
                    isLoading: viewModel.isLoading,
                    hasError: viewModel.hasError,
                    errorMessage: viewModel.errorMessage,
                    onTap: viewModel.startAuthFlow
                )
            }
            .padding(AppConstants.UI.padding)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .navigationBarHidden(true)
    }
}

// MARK: - Login Button Component
private struct LoginButton: View {
    let isLoading: Bool
    let hasError: Bool
    let errorMessage: String?
    let onTap: () -> Void
    
    var body: some View {
        VStack(spacing: 12) {
            Button(action: onTap) {
                HStack(spacing: 20) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(0.8)
                    } else {
                        Text(AppConstants.Strings.loginButtonText)
                            .font(.title3)
                        Image("x_logo")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 16, height: 16)
                    }
                }
            }
            .disabled(isLoading)
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color.black)
            .foregroundColor(.white)
            .cornerRadius(AppConstants.UI.cornerRadius)
            .opacity(isLoading ? 0.7 : 1.0)
            .padding(.horizontal)
            
            // Error message
            if hasError, let errorMessage = errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
            }
        }
    }
}

// MARK: - Preview
#Preview {
    LoginView()
} 
