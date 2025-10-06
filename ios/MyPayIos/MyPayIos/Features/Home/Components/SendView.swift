//
//  SendView.swift
//  MyPayIos
//
//  Created by Claude Code on 16.09.25.
//

import SwiftUI

// MARK: - Send View
struct SendView: View {
    let userWalletAddress: String
    let amount: String?
    let selectedChain: BlockchainChain
    let onBackClick: () -> Void
    
    @State private var isAnimating = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("Pay")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.white)
                
                Spacer()
            }
            .padding(.horizontal, 32)
            .padding(.vertical, 24)
            
            // Amount Display
            if let amount = amount, !amount.isEmpty {
                Text(amount)
                    .font(.system(size: 40, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.vertical, 32)
            }
            
            Spacer()
            
            // NFC Icon with animation
            Image(systemName: "wave.3.right")
                .font(.system(size: 48))
                .foregroundColor(AppConstants.Colors.primary)
                .scaleEffect(isAnimating ? 1.2 : 1.0)
                .animation(.easeInOut(duration: 0.7).repeatForever(autoreverses: true), value: isAnimating)
                .onAppear {
                    isAnimating = true
                }
            
            Text("Hold near NFC reader")
                .font(.system(size: 16))
                .foregroundColor(.white)
                .padding(.vertical, 16)
            
            // Card View
            CardView(walletAddress: userWalletAddress, selectedChain: selectedChain)
                .scaleEffect(isAnimating ? 1.02 : 1.0)
                .animation(.easeInOut(duration: 0.7).repeatForever(autoreverses: true), value: isAnimating)
                .padding(.vertical, 16)
            
            Spacer()
            
            // Cancel Button
            Button("Cancel") {
                onBackClick()
            }
            .font(.system(size: 16))
            .foregroundColor(.white)
            .padding(.bottom, 16)
            .frame(maxWidth: .infinity)
        }
        .padding(.horizontal, 16)
        .background(Color("DarkBackground"))
    }
}

// MARK: - Card View
struct CardView: View {
    let walletAddress: String
    let selectedChain: BlockchainChain
    
    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Image(selectedChain == .solana ? "solana_logo" : "ethereum_logo")
                    .resizable()
                    .frame(width: 32, height: 32)
                
                VStack(alignment: .leading) {
                    Text(selectedChain.displayName)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white)
                    
                    Text(selectedChain.displayName)
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                Spacer()
            }
            
            HStack {
                Text("Wallet Address:")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.7))
                
                Spacer()
                
                Text(walletAddress.prefix(8) + "..." + walletAddress.suffix(8))
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
            }
        }
        .padding(16)
        .background(Color.white.opacity(0.1))
        .cornerRadius(12)
    }
}

// MARK: - Preview
#Preview {
    SendView(
        userWalletAddress: "1234567890abcdefghijklmnopqrstuvwxyz",
        amount: "1.5 SOL",
        selectedChain: .solana,
        onBackClick: {}
    )
}
