//
//  SolanaPayURLParser.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 08.10.25.
//

import Foundation

struct SolanaPayURLParser {
    
    static func parseURL(_ urlString: String) throws -> TransferRequestURLFields {
        // Check URL length
        guard urlString.count <= 2048 else {
            throw ParseURLError.urlLengthInvalid
        }
        
        guard let url = URL(string: urlString) else {
            throw ParseURLError.protocolInvalid
        }
        
        // Check protocol
        let expectedScheme = SolanaPayConstants.solanaProtocol.replacingOccurrences(of: ":", with: "")
        guard url.scheme == expectedScheme else {
            throw ParseURLError.protocolInvalid
        }
        
        // Extract recipient from the path
        let recipientPath = url.path.isEmpty ? url.host ?? "" : url.path.replacingOccurrences(of: "/", with: "")
        
        // Handle the case where recipient might be in the scheme-specific part
        let recipient: String
        if recipientPath.isEmpty {
            // For URLs like "solana:5BUaqabshK42awJpVQyHh31wE4vNPAkjbPe9JZQXTNr7?amount=1"
            let schemeSpecificPart = urlString.replacingOccurrences(of: SolanaPayConstants.solanaProtocol, with: "")
            let components = schemeSpecificPart.components(separatedBy: "?")
            recipient = components.first ?? ""
        } else {
            recipient = recipientPath
        }
        
        guard !recipient.isEmpty else {
            throw ParseURLError.recipientMissing
        }
        
        // Validate recipient address (basic validation)
        guard isValidSolanaAddress(recipient) else {
            throw ParseURLError.recipientInvalid
        }
        
        // Parse query parameters
        guard let components = URLComponents(string: urlString),
              let queryItems = components.queryItems else {
            throw ParseURLError.amountMissing
        }
        
        // Extract amount
        guard let amountString = queryItems.first(where: { $0.name == "amount" })?.value,
              let amountValue = Decimal(string: amountString),
              amountValue > 0 else {
            throw ParseURLError.amountMissing
        }
        
        // Extract optional parameters
        let splToken = queryItems.first(where: { $0.name == "spl-token" })?.value
        let references = queryItems.filter { $0.name == "reference" }.compactMap { $0.value }
        let label = queryItems.first(where: { $0.name == "label" })?.value?.removingPercentEncoding
        let message = queryItems.first(where: { $0.name == "message" })?.value?.removingPercentEncoding
        let memo = queryItems.first(where: { $0.name == "memo" })?.value?.removingPercentEncoding
        
        // Validate SPL token if present
        if let splToken = splToken, !isValidSolanaAddress(splToken) {
            throw ParseURLError.splTokenInvalid
        }
        
        // Validate references if present
        for reference in references {
            if !isValidSolanaAddress(reference) {
                throw ParseURLError.referenceInvalid
            }
        }
        
        return TransferRequestURLFields(
            recipient: recipient,
            amount: amountValue,
            tokenDecimal: getTokenDecimal(mint: splToken),
            splToken: splToken,
            reference: references.isEmpty ? nil : references,
            label: label,
            message: message,
            memo: memo
        )
    }
    
    // MARK: - Private Helper Methods
    
    private static func isValidSolanaAddress(_ address: String) -> Bool {
        // Basic Solana address validation
        // Solana addresses are base58 encoded and typically 32-44 characters long
        let addressRegex = "^[1-9A-HJ-NP-Za-km-z]{32,44}$"
        let predicate = NSPredicate(format: "SELF MATCHES %@", addressRegex)
        return predicate.evaluate(with: address)
    }
    
    private static func getTokenDecimal(mint: String?) -> Int {
        // Default to 9 decimals for SOL and most SPL tokens
        // In a real implementation, you might want to fetch this from the blockchain
        return 9
    }
}
