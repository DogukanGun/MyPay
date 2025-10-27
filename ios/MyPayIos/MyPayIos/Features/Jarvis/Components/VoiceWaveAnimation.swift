//
//  VoiceWaveAnimation.swift
//  MyPayIos
//
//  Created by AI Assistant on 16.10.25.
//

import SwiftUI

// MARK: - Voice Wave Animation
struct VoiceWaveAnimation: View {
    let isActive: Bool
    @State private var phase: CGFloat = 0
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Multiple wave circles
                ForEach(0..<3) { index in
                    Circle()
                        .stroke(
                            LinearGradient(
                                colors: [
                                    Color(red: 0.56, green: 0.77, blue: 0.99).opacity(0.3),
                                    Color(red: 0.35, green: 0.58, blue: 0.93).opacity(0.1)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 2
                        )
                        .frame(
                            width: geometry.size.width * waveScale(for: index),
                            height: geometry.size.height * waveScale(for: index)
                        )
                        .opacity(waveOpacity(for: index))
                }
                
                // Center circle
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [
                                Color(red: 0.56, green: 0.77, blue: 0.99),
                                Color(red: 0.35, green: 0.58, blue: 0.93)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(
                        width: geometry.size.width * 0.3,
                        height: geometry.size.height * 0.3
                    )
                    .shadow(color: Color(red: 0.56, green: 0.77, blue: 0.99).opacity(0.5), radius: 20)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .onAppear {
            if isActive {
                startAnimation()
            }
        }
        .onChange(of: isActive) { newValue in
            if newValue {
                startAnimation()
            } else {
                withAnimation(.easeOut(duration: 0.3)) {
                    phase = 0
                }
            }
        }
    }
    
    private func waveScale(for index: Int) -> CGFloat {
        guard isActive else { return 0.3 }
        let baseScale = 0.4 + CGFloat(index) * 0.2
        let animationOffset = sin(phase + CGFloat(index) * .pi / 3) * 0.1
        return baseScale + animationOffset
    }
    
    private func waveOpacity(for index: Int) -> Double {
        guard isActive else { return 0 }
        let baseOpacity = 0.7 - Double(index) * 0.2
        let animationOffset = sin(phase + CGFloat(index) * .pi / 3) * 0.2
        return max(0, baseOpacity + animationOffset)
    }
    
    private func startAnimation() {
        withAnimation(
            .linear(duration: 2.0)
            .repeatForever(autoreverses: false)
        ) {
            phase = .pi * 2
        }
    }
}

// MARK: - Preview
#Preview {
    VoiceWaveAnimation(isActive: true)
        .frame(width: 300, height: 300)
        .background(Color.gray.opacity(0.1))
}

