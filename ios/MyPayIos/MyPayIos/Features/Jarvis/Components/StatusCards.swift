//
//  StatusCards.swift
//  MyPayIos
//
//  Created by AI Assistant on 16.10.25.
//

import SwiftUI

// MARK: - Card Data
struct CardData: Identifiable, Equatable {
    let id = UUID()
    let message: String
    let type: CardType
    
    enum CardType {
        case info
        case success
        case error
        case processing
        
        var backgroundColor: Color {
            switch self {
            case .info:
                return Color(red: 0.35, green: 0.58, blue: 0.93).opacity(0.1)
            case .success:
                return Color.green.opacity(0.1)
            case .error:
                return Color.red.opacity(0.1)
            case .processing:
                return Color.orange.opacity(0.1)
            }
        }
        
        var textColor: Color {
            switch self {
            case .info:
                return Color(red: 0.35, green: 0.58, blue: 0.93)
            case .success:
                return Color.green
            case .error:
                return Color.red
            case .processing:
                return Color.orange
            }
        }
    }
}

// MARK: - Status Cards View
struct StatusCards: View {
    let cardData: CardData?
    let onCardDismiss: () -> Void
    
    var body: some View {
        if let card = cardData {
            VStack {
                HStack {
                    Text(card.message)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(card.type.textColor)
                        .multilineTextAlignment(.leading)
                    
                    Spacer()
                    
                    Button(action: onCardDismiss) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(card.type.textColor.opacity(0.6))
                            .font(.system(size: 20))
                    }
                }
                .padding()
                .background(card.type.backgroundColor)
                .cornerRadius(12)
                .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
            }
            .padding(.horizontal)
            .transition(.move(edge: .bottom).combined(with: .opacity))
            .animation(.spring(response: 0.3, dampingFraction: 0.8), value: card.id)
        }
    }
}

// MARK: - Preview
#Preview {
    VStack {
        StatusCards(
            cardData: CardData(message: "Processing your request...", type: .processing),
            onCardDismiss: {}
        )
        
        StatusCards(
            cardData: CardData(message: "Success! Your request was completed.", type: .success),
            onCardDismiss: {}
        )
        
        StatusCards(
            cardData: CardData(message: "Error: Something went wrong.", type: .error),
            onCardDismiss: {}
        )
    }
}

