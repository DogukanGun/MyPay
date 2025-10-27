//
//  JarvisView.swift
//  MyPayIos
//
//  Created by AI Assistant on 16.10.25.
//

import SwiftUI

// MARK: - Jarvis View
struct JarvisView: View {
    @StateObject private var viewModel = JarvisViewModel()
    @State private var isPressed: Bool = false
    
    var body: some View {
        ZStack {
            // Background
            AppConstants.Colors.background
                .ignoresSafeArea(.all)
            
            VStack(spacing: 0) {
                // Top Bar
                TopBar(
                    title: "Jarvis",
                    showLogoutButton: false
                )
                
                // Main Content
                ZStack(alignment: .bottom) {
                    // Center Content
                    VStack(spacing: 24) {
                        Spacer()
                        
                        // Wave Animation
                        voiceAnimationView
                        
                        // Status Text
                        statusTextView
                        
                        Spacer()
                    }
                    
                    // Status Cards at bottom
                    StatusCards(
                        cardData: viewModel.currentCard,
                        onCardDismiss: viewModel.hideCard
                    )
                    .padding(.bottom, 100)
                }
            }
        }
        .navigationBarHidden(true)
        .alert("Microphone Permission", isPresented: .constant(viewModel.viewState == .askPermission)) {
            Button("Allow") {
                viewModel.requestPermission()
            }
            Button("Cancel", role: .cancel) {
                viewModel.handleAudioPermissionResult(false)
            }
        } message: {
            Text("Jarvis needs access to your microphone to listen to your voice commands.")
        }
        .alert("Confirm Your Message", isPresented: confirmationBinding) {
            Button("Send") {
                viewModel.confirmAndSend()
            }
            Button("Cancel", role: .cancel) {
                viewModel.cancelConfirmation()
            }
        } message: {
            if case .confirming(let transcription) = viewModel.viewState {
                Text("You said: \"\(transcription)\"\n\nDo you approve this to be sent?")
            }
        }
    }
    
    // MARK: - Computed Properties
    private var confirmationBinding: Binding<Bool> {
        Binding(
            get: {
                if case .confirming = viewModel.viewState {
                    return true
                }
                return false
            },
            set: { _ in }
        )
    }
    
    private var pulseScale: CGFloat {
        viewModel.viewState.isActive ? 1.1 : 1.0
    }
    
    private var isButtonDisabled: Bool {
        switch viewModel.viewState {
        case .processing, .confirming:
            return true
        default:
            return false
        }
    }
    
    // MARK: - Voice Animation View
    private var voiceAnimationView: some View {
        Button(action: handleTap) {
            ZStack {
                // Background circle with gradient
                Circle()
                    .fill(
                        Color(red: 0.56, green: 0.77, blue: 0.99)
                            .opacity(viewModel.isListening ? 0.1 : 0.05)
                    )
                    .frame(width: 300, height: 300)
                
                // Wave Animation
                VoiceWaveAnimation(isActive: viewModel.viewState.isActive)
                    .frame(width: 300, height: 300)
            }
            .scaleEffect(isPressed ? 0.95 : 1.0)
            .scaleEffect(viewModel.viewState.isActive ? pulseScale : 1.0)
            .animation(.spring(response: 0.3, dampingFraction: 0.6), value: isPressed)
            .animation(
                viewModel.viewState.isActive
                    ? .easeInOut(duration: 1.0).repeatForever(autoreverses: true)
                    : .easeInOut(duration: 0.3),
                value: viewModel.viewState.isActive
            )
        }
        .buttonStyle(PressButtonStyle(isPressed: $isPressed))
        .disabled(isButtonDisabled)
    }
    
    // MARK: - Status Text View
    private var statusTextView: some View {
        Text(viewModel.viewState.displayText)
            .font(.system(size: 18, weight: .medium))
            .foregroundColor(Color(red: 0.22, green: 0.25, blue: 0.32))
            .multilineTextAlignment(.center)
            .padding(.horizontal, 32)
    }
    
    // MARK: - Actions
    private func handleTap() {
        switch viewModel.viewState {
        case .listening:
            viewModel.stopListening()
        case .idle:
            viewModel.startListening()
        case .askPermission:
            viewModel.requestPermission()
        case .success, .error:
            viewModel.resetToIdle()
        default:
            break
        }
    }
}

// MARK: - Press Button Style
struct PressButtonStyle: ButtonStyle {
    @Binding var isPressed: Bool
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .onChange(of: configuration.isPressed) { newValue in
                isPressed = newValue
            }
    }
}

// MARK: - Preview
#Preview {
    JarvisView()
}

