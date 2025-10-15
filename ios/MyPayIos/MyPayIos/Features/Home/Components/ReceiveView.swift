//
//  ReceiveView.swift
//  MyPayIos
//
//  Created by Claude Code on 16.09.25.
//

import SwiftUI
import CoreImage
import CoreImage.CIFilterBuiltins

// MARK: - Receive View
struct ReceiveView: View {
    let selectedChain: BlockchainChain
    let paymentQRCodeURL: String?
    let onChainChanged: (BlockchainChain) -> Void
    let onBackClick: () -> Void
    let onContinueClick: (Double, String) -> Void
    
    @State private var amount: Double = 0.0
    @State private var recipient: String = ""
    @State private var showChainPicker: Bool = false
    @State private var showQRCode: Bool = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                headerSection
                
                if let qrCodeURL = paymentQRCodeURL {
                    qrCodeSection(url: qrCodeURL)
                } else {
                    inputSection
                    balanceSection
                    feeSection
                    Spacer(minLength: 32)
                    continueButtonSection
                }
            }
        }
        .background(Color("DarkBackground"))
    }
    
    // MARK: - View Components
    
    private var headerSection: some View {
        HStack {
            Text("Convert")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            Spacer()
        }
        .padding(.horizontal, 32)
        .padding(.vertical, 24)
    }

    
    private var inputSection: some View {
        VStack(spacing: 16) {
            HStack(spacing: 8) {
                currencySelector
                    .frame(maxWidth: .infinity)
                amountInput
                    .frame(maxWidth: .infinity)
            }
            recipientInput
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
    
    private var currencySelector: some View {
        Button(action: {
            showChainPicker.toggle()
        }) {
            HStack {
                HStack(spacing: 8) {
                    Image(getChainImageName(for: selectedChain))
                        .resizable()
                        .frame(width: 24, height: 24)
                    
                    Text(selectedChain.displayName)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                }
                
                Spacer()
                
                Image(systemName: "chevron.down")
                    .font(.system(size: 12))
                    .foregroundColor(.white)
            }
            .padding(12)
            .background(Color.white.opacity(0.1))
            .cornerRadius(16)
        }
        .confirmationDialog("Select Blockchain", isPresented: $showChainPicker) {
            ForEach(BlockchainChain.allCases, id: \.self) { chain in
                Button(chain.displayName) {
                    onChainChanged(chain)
                }
            }
        }
    }
    
    private var numberFormatter: NumberFormatter {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = 2
        return formatter
    }
    
    private var amountInput: some View {
        TextField("Amount", value: $amount, formatter: numberFormatter)
            .keyboardType(.decimalPad)
            .padding(12)
            .background(Color.white.opacity(0.1))
            .foregroundColor(.white)
            .cornerRadius(16)
            .accentColor(.white)
    }
    
    private var recipientInput: some View {
        TextField("Recipient", text: $recipient)
            .padding(12)
            .background(Color.white.opacity(0.1))
            .foregroundColor(.white)
            .cornerRadius(16)
            .accentColor(.white)
    }
    
    private var balanceSection: some View {
        HStack {
            Text("Available balance")
                .font(.system(size: 14))
                .foregroundColor(.white)
            
            Spacer()
            
            Text("$112,340.00")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.white)
        }
        .padding(16)
        .background(Color.white.opacity(0.1))
        .cornerRadius(16)
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
    
    private var feeSection: some View {
        HStack {
            Text("Exchange fee")
                .font(.system(size: 14))
                .foregroundColor(.white)
            
            Spacer()
            
            Text("$20")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.white)
        }
        .padding(16)
        .background(Color.white.opacity(0.1))
        .cornerRadius(16)
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
    
    private var continueButtonSection: some View {
        Button(action: {
            onContinueClick(amount, recipient)
        }) {
            Text("Generate QR Code")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(Color.white)
                .cornerRadius(12)
        }
        .disabled(amount == 0.0 || recipient.isEmpty)
        .opacity((amount == 0.0 || recipient.isEmpty) ? 0.6 : 1.0)
        .padding(.horizontal, 16)
        .padding(.bottom, 64)
    }
    
    private func qrCodeSection(url: String) -> some View {
        VStack(spacing: 24) {
            Text("Solana Pay QR Code")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
                .padding(.top, 24)
            
            VStack(spacing: 16) {
                if let qrCodeImage = generateQRCode(from: url) {
                    Image(uiImage: qrCodeImage)
                        .interpolation(.none)
                        .resizable()
                        .frame(width: 200, height: 200)
                        .background(Color.white)
                        .cornerRadius(16)
                } else {
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.white)
                        .frame(width: 200, height: 200)
                        .overlay(
                            Text("Failed to generate QR code")
                                .foregroundColor(.black)
                                .font(.system(size: 14))
                                .multilineTextAlignment(.center)
                        )
                }
                
                Text("Scan this QR code with a Solana Pay compatible wallet")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            .padding(.horizontal, 24)
            
            Button(action: {
                // Copy URL to clipboard
                UIPasteboard.general.string = url
            }) {
                HStack {
                    Image(systemName: "doc.on.clipboard")
                    Text("Copy Payment Link")
                }
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(Color.blue)
                .cornerRadius(12)
            }
            .padding(.horizontal, 16)
            
            Button(action: onBackClick) {
                Text("Create New Payment")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(Color.white)
                    .cornerRadius(12)
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 64)
        }
    }
    
    private func generateQRCode(from string: String) -> UIImage? {
        let data = string.data(using: String.Encoding.ascii)
        
        if let filter = CIFilter(name: "CIQRCodeGenerator") {
            filter.setValue(data, forKey: "inputMessage")
            let transform = CGAffineTransform(scaleX: 3, y: 3)
            
            if let output = filter.outputImage?.transformed(by: transform) {
                let context = CIContext()
                if let cgImage = context.createCGImage(output, from: output.extent) {
                    return UIImage(cgImage: cgImage)
                }
            }
        }
        
        return nil
    }
    
    private func getChainImageName(for chain: BlockchainChain) -> String {
        switch chain {
        case .solana:
            return "solana_logo"
        case .ethereum:
            return "ethereum_logo"
        }
    }
}

// MARK: - Preview
#Preview {
    ReceiveView(
        selectedChain: .solana,
        paymentQRCodeURL: nil,
        onChainChanged: { _ in },
        onBackClick: {},
        onContinueClick: { _, _ in }
    )
}
