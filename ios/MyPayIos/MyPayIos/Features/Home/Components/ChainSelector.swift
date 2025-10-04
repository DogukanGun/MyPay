//
//  ChainSelector.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import SwiftUI

enum BlockchainChain: String, CaseIterable {
    case ethereum = "ETH"
    case solana = "SOL"
    
    var displayName: String {
        switch self {
        case .ethereum:
            return "Ethereum"
        case .solana:
            return "Solana"
        }
    }
    
    var iconName: String {
        switch self {
        case .ethereum:
            return "e.circle.fill"
        case .solana:
            return "s.circle.fill"
        }
    }
    
    var iconColor: Color {
        switch self {
        case .ethereum:
            return .blue
        case .solana:
            return .pink
        }
    }
}

struct ChainSelector: View {
    let selectedChain: BlockchainChain
    let onChainChanged: (BlockchainChain) -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            ForEach(BlockchainChain.allCases, id: \.self) { chain in
                ChainButton(
                    chain: chain,
                    isSelected: selectedChain == chain,
                    onTap: {
                        onChainChanged(chain)
                    }
                )
            }
            
            Spacer()
        }
        .padding(.horizontal, 16)
    }
}

struct ChainButton: View {
    let chain: BlockchainChain
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 8) {
                Image(systemName: chain.iconName)
                    .foregroundColor(chain.iconColor)
                    .font(.system(size: 16, weight: .medium))
                
                Text(chain.rawValue)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(isSelected ? .white : .black)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(isSelected ? Color.blue : Color.gray.opacity(0.1))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(isSelected ? Color.blue : Color.gray.opacity(0.3), lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    ChainSelector(
        selectedChain: .solana,
        onChainChanged: { _ in }
    )
    .padding()
}
