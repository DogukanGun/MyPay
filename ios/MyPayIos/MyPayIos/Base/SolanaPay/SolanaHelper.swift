//
//  SolanaHelper.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 08.10.25.
//

import Foundation

// MARK: - Solana Helper Protocol
protocol SolanaHelperProtocol {
    func prepareSolanaPay(
        transferRequestField: TransferRequestURLFields,
        completion: @escaping (Result<URL, Error>) -> Void
    )
    
    func receiveSolanaPayAndMakePayment(
        paymentUrl: String,
        privateKey: String,
        completion: @escaping (Result<SolanaTransaction, Error>) -> Void
    )
}

// MARK: - Solana Helper Implementation
class SolanaHelper: SolanaHelperProtocol {
    
    static let shared = SolanaHelper()
    
    private let walletManager: WalletManager
    
    init(walletManager: WalletManager = WalletManager.shared) {
        self.walletManager = walletManager
    }
    
    // MARK: - Public Methods
    
    func prepareSolanaPay(
        transferRequestField: TransferRequestURLFields,
        completion: @escaping (Result<URL, Error>) -> Void
    ) {
        print("DEBUG: SolanaHelper.prepareSolanaPay called")
        print("DEBUG: Input fields - recipient: '\(transferRequestField.recipient)', amount: \(transferRequestField.amount)")
        
        guard let url = SolanaPayURLEncoder.encodeURL(fields: transferRequestField) else {
            print("DEBUG: SolanaPayURLEncoder.encodeURL returned nil")
            completion(.failure(AppError.validation(.invalidFormat("Wrong URI format"))))
            return
        }
        
        print("DEBUG: SolanaHelper.prepareSolanaPay successful, URL: \(url.absoluteString)")
        completion(.success(url))
    }
    
    func receiveSolanaPayAndMakePayment(
        paymentUrl: String,
        privateKey: String,
        completion: @escaping (Result<SolanaTransaction, Error>) -> Void
    ) {
        Task {
            do {
                // Parse the payment URL
                let transferFields = try SolanaPayURLParser.parseURL(paymentUrl)
                
                // Get the sender's public key from the private key
                guard let senderPublicKey = walletManager.getPublicAddress(for: .solana) else {
                    await MainActor.run {
                        completion(.failure(AppError.wallet(.walletNotFound)))
                    }
                    return
                }
                
                // Get recent blockhash (in a real implementation, this would be fetched from the network)
                let recentBlockhash = await getRecentBlockhash()
                
                // Create the transaction
                let transaction = try await SolanaPayTransferCreator.createTransfer(
                    sender: senderPublicKey,
                    fields: transferFields,
                    recentBlockhash: recentBlockhash
                )
                
                await MainActor.run {
                    completion(.success(transaction))
                }
                
            } catch {
                await MainActor.run {
                    completion(.failure(error))
                }
            }
        }
    }
    
    // MARK: - Private Methods
    
    private func makePayment(
        privateKey: String,
        transferRequestField: TransferRequestURLFields,
        completion: @escaping (Result<SolanaTransaction, Error>) -> Void
    ) {
        Task {
            do {
                guard let senderPublicKey = walletManager.getPublicAddress(for: .solana) else {
                    await MainActor.run {
                        completion(.failure(AppError.wallet(.walletNotFound)))
                    }
                    return
                }
                
                // Get recent blockhash
                let recentBlockhash = await getRecentBlockhash()
                
                // Create the transaction
                let transaction = try await SolanaPayTransferCreator.createTransfer(
                    sender: senderPublicKey,
                    fields: transferRequestField,
                    recentBlockhash: recentBlockhash
                )
                
                await MainActor.run {
                    completion(.success(transaction))
                }
                
            } catch {
                await MainActor.run {
                    completion(.failure(error))
                }
            }
        }
    }
    
    private func getRecentBlockhash() async -> String {
        // In a real implementation, this would fetch the recent blockhash from the Solana network
        // For now, we'll return a placeholder
        return "11111111111111111111111111111111111111111111"
    }
}

// MARK: - App Error Extensions
extension AppError {
    enum WalletError: Error, LocalizedError {
        case walletNotFound
        case invalidPrivateKey
        case transactionFailed
        
        var errorDescription: String? {
            switch self {
            case .walletNotFound:
                return "Wallet not found"
            case .invalidPrivateKey:
                return "Invalid private key"
            case .transactionFailed:
                return "Transaction failed"
            }
        }
    }
    
    static func wallet(_ error: WalletError) -> AppError {
        return AppError.unknown(error.localizedDescription)
    }
}
