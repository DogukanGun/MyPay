//
//  SplashView.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 11.09.25.
//

import SwiftUI

// MARK: - Splash View
struct SplashView: View {
    @EnvironmentObject private var navigationCoordinator: NavigationCoordinator
    @StateObject private var viewModel: SplashViewModel
    @State private var scale: CGFloat = 0.9
    @State private var opacity: Double = 0.0
    
    let onCompletion: (Destination) -> Void
    
    init(onCompletion: @escaping (Destination) -> Void) {
        self.onCompletion = onCompletion
        self._viewModel = StateObject(wrappedValue: SplashViewModel())
    }
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient.iconGradient
                .ignoresSafeArea(.all)
            
            // Logo with animations
            Image("mypay_logo")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: AppConstants.UI.logoSize.width, height: AppConstants.UI.logoSize.height)
                .scaleEffect(scale)
                .opacity(opacity)
                .onAppear {
                    withAnimation(.easeInOut(duration: 1.0)) {
                        opacity = 1.0
                    }
                    withAnimation(
                        .easeInOut(duration: 2.0)
                        .repeatForever(autoreverses: true)
                    ) {
                        scale = 1.2
                    }
                }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .navigationBarHidden(true)
        .onReceive(viewModel.$viewState) { state in
            handleViewState(state)
        }
    }
    
    // MARK: - Private Methods
    private func handleViewState(_ state: SplashVS?) {
        guard let state = state else { return }
        
        switch state {
        case .startApp(let destination):
            onCompletion(destination)
            
        case .closeApp:
            viewModel.closeApp()
        }
    }
}

// MARK: - Preview
#Preview {
    SplashView { _ in }
}

