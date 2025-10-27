//
//  SendView.swift
//  MyPayIos
//
//  Created by Claude Code on 16.09.25.
//

import SwiftUI
import AVFoundation
import CoreImage

// MARK: - Send View
struct SendView: View {
    let userWalletAddress: String
    let amount: String?
    let selectedChain: BlockchainChain
    let onBackClick: () -> Void
    
    @State private var showingQRScanner = false
    @State private var scannedPaymentURL: String?
    @State private var paymentConfirmed = false
    
    var body: some View {
        VStack(spacing: 24) {
            // Header
            HStack {
                Text("Pay")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.white)
                
                Spacer()
            }
            .padding(.horizontal, 32)
            .padding(.vertical, 24)
            
            if let scannedURL = scannedPaymentURL {
                // Payment Confirmation View
                paymentConfirmationView(url: scannedURL)
            } else {
                // QR Scanner Instructions
                qrScannerInstructionsView
            }
            
            Spacer()
            
            // Action Buttons
            VStack(spacing: 16) {
                if scannedPaymentURL == nil {
                    Button(action: {
                        showingQRScanner = true
                    }) {
                        HStack {
                            Image(systemName: "qrcode.viewfinder")
                                .font(.system(size: 20))
                            Text("Scan QR Code")
                                .font(.system(size: 16, weight: .medium))
                        }
                        .foregroundColor(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.white)
                        .cornerRadius(12)
                    }
                    .padding(.horizontal, 16)
                }
                
                Button("Cancel") {
                    onBackClick()
                }
                .font(.system(size: 16))
                .foregroundColor(.white)
                .padding(.bottom, 16)
                .frame(maxWidth: .infinity)
            }
        }
        .padding(.horizontal, 16)
        .background(Color("DarkBackground"))
        .sheet(isPresented: $showingQRScanner) {
            QRCodeScannerView { result in
                handleQRScanResult(result)
            }
        }
    }
    
    // MARK: - View Components
    
    private var qrScannerInstructionsView: some View {
        VStack(spacing: 24) {
            Image(systemName: "qrcode")
                .font(.system(size: 64))
                .foregroundColor(AppConstants.Colors.primary)
                .padding(.top, 32)
            
            VStack(spacing: 8) {
                Text("Scan Solana Pay QR Code")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                
                Text("Scan a QR code from a merchant or payment request")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.7))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            
            // Wallet Info Card
            CardView(walletAddress: userWalletAddress, selectedChain: selectedChain)
                .padding(.horizontal, 16)
        }
    }
    
    private func paymentConfirmationView(url: String) -> some View {
        VStack(spacing: 24) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 64))
                .foregroundColor(.green)
            
            Text("Payment Request Detected")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            
            VStack(spacing: 12) {
                Text("Solana Pay URL:")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.7))
                
                Text(url.prefix(50) + "...")
                    .font(.system(size: 12, weight: .regular))
                    .foregroundColor(.white)
                    .padding(12)
                    .background(Color.white.opacity(0.1))
                    .cornerRadius(8)
            }
            .padding(.horizontal, 16)
            
            Button(action: {
                processPayment(url: url)
            }) {
                Text("Confirm Payment")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(Color.green)
                    .cornerRadius(12)
            }
            .padding(.horizontal, 16)
            
            Button(action: {
                scannedPaymentURL = nil
            }) {
                Text("Scan Different QR Code")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.7))
            }
        }
    }
    
    // MARK: - Actions
    
    private func handleQRScanResult(_ result: String) {
        if result.hasPrefix("solana:") {
            scannedPaymentURL = result
        } else {
            // Handle invalid QR code
            print("Invalid Solana Pay QR code: \(result)")
        }
    }
    
    private func processPayment(url: String) {
        // Here you would integrate with your payment processing logic
        // For now, just show success and close
        paymentConfirmed = true
        
        // Simulate payment processing
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            onBackClick()
        }
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
