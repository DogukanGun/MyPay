//
//  SolanaPayTypes.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 08.10.25.
//

import Foundation

// MARK: - Type Aliases
typealias Recipient = String
typealias Amount = Decimal
typealias SPLToken = String
typealias Reference = String
typealias References = [Reference]
typealias Label = String
typealias Message = String
typealias Memo = String
typealias Link = URL

// MARK: - Transaction Request URL Fields
struct TransactionRequestURLFields {
    let link: Link
    let label: Label?
    let message: Message?
}

// MARK: - Transfer Request URL Fields
struct TransferRequestURLFields {
    let recipient: Recipient
    let amount: Amount
    let tokenDecimal: Int
    let splToken: SPLToken?
    let reference: References?
    let label: Label?
    let message: Message?
    let memo: Memo?
    
    init(recipient: Recipient, 
         amount: Amount, 
         tokenDecimal: Int = 9,
         splToken: SPLToken? = nil,
         reference: References? = nil,
         label: Label? = nil,
         message: Message? = nil,
         memo: Memo? = nil) {
        self.recipient = recipient
        self.amount = amount
        self.tokenDecimal = tokenDecimal
        self.splToken = splToken
        self.reference = reference
        self.label = label
        self.message = message
        self.memo = memo
    }
}

// MARK: - Parse URL Error
enum ParseURLError: Error, LocalizedError {
    case urlLengthInvalid
    case protocolInvalid
    case recipientMissing
    case recipientInvalid
    case amountMissing
    case amountInvalid
    case splTokenInvalid
    case referenceInvalid
    
    var errorDescription: String? {
        switch self {
        case .urlLengthInvalid:
            return "URL length invalid"
        case .protocolInvalid:
            return "Protocol invalid"
        case .recipientMissing:
            return "Recipient address is missing from URL path"
        case .recipientInvalid:
            return "Recipient address is invalid"
        case .amountMissing:
            return "Amount missing"
        case .amountInvalid:
            return "Amount invalid"
        case .splTokenInvalid:
            return "SPL Token invalid"
        case .referenceInvalid:
            return "Reference invalid"
        }
    }
}
