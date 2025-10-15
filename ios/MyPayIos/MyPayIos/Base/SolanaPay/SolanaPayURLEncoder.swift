//
//  SolanaPayURLEncoder.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 08.10.25.
//

import Foundation

struct SolanaPayURLEncoder {
    
    static func encodeURL(fields: TransferRequestURLFields) -> URL? {
        return encodeTransferRequestURL(fields: fields)
    }
    
    private static func encodeTransferRequestURL(fields: TransferRequestURLFields) -> URL? {
        // Validate recipient is a valid base58 address (should be 32-44 characters)
        let recipient = fields.recipient.trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Debug logging
        print("DEBUG: Recipient address: '\(recipient)'")
        print("DEBUG: Recipient length: \(recipient.count)")
        
        // Basic validation - check if recipient is not empty
        guard !recipient.isEmpty else {
            print("DEBUG: Recipient validation failed - empty recipient")
            return nil
        }
        
        // For now, let's be very lenient with validation to debug the core issue
        print("DEBUG: Recipient validation passed")
        
        let pathname = recipient
        let baseUri = "\(SolanaPayConstants.solanaProtocol)\(pathname)"
        
        var queryParams: [String] = []
        
        // Add amount - must be in "user units" (SOL/USDC, not lamports)
        // According to Solana Pay spec: decimal places limited to 9
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = min(9, fields.tokenDecimal)
        formatter.minimumFractionDigits = 0
        formatter.usesGroupingSeparator = false
        
        if let formattedAmount = formatter.string(from: NSDecimalNumber(decimal: fields.amount)) {
            print("DEBUG: Formatted amount: \(formattedAmount)")
            queryParams.append("amount=\(formattedAmount)")
        } else {
            print("DEBUG: Failed to format amount: \(fields.amount)")
        }
        
        // Add optional parameters
        if let splToken = fields.splToken?.trimmingCharacters(in: .whitespacesAndNewlines),
           !splToken.isEmpty {
            queryParams.append("spl-token=\(splToken)")
        }
        
        if let references = fields.reference {
            for reference in references {
                queryParams.append("reference=\(reference)")
            }
        }
        
        if let label = fields.label,
           let encodedLabel = label.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            queryParams.append("label=\(encodedLabel)")
        }
        
        if let message = fields.message,
           let encodedMessage = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            queryParams.append("message=\(encodedMessage)")
        }
        
        if let memo = fields.memo,
           let encodedMemo = memo.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            queryParams.append("memo=\(encodedMemo)")
        }
        
        let uriString = baseUri + (queryParams.isEmpty ? "" : "?\(queryParams.joined(separator: "&"))")
        
        print("DEBUG: Generated URI: \(uriString)")
        
        let url = URL(string: uriString)
        if url == nil {
            print("DEBUG: Failed to create URL from string: \(uriString)")
        }
        
        return url
    }
}
