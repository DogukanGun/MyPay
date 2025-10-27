//
//  WalletManager.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import Foundation

class WalletManager {
    static let shared = WalletManager()
    
    private let biometricHelper = BiometricHelper.shared
    private let publicKeyStorage = TokenStorage.shared // Reuse existing secure storage
    
    private init() {}
    
    // MARK: - Wallet Storage
    func storeWallets(ethWallet: WalletInfo?, solanaWallet: WalletInfo?, completion: @escaping (Bool) -> Void) {
        var storedCount = 0
        let totalWallets = (ethWallet != nil ? 1 : 0) + (solanaWallet != nil ? 1 : 0)
        
        if totalWallets == 0 {
            completion(true)
            return
        }
        
        // Store ETH wallet
        if let ethWallet = ethWallet {
            biometricHelper.storePrivateKey(ethWallet.privateKey, for: .ethereum) { [weak self] success in
                if success {
                    self?.storePublicKey(ethWallet.publicAddress, for: .ethereum)
                }
                storedCount += 1
                if storedCount == totalWallets {
                    completion(success)
                }
            }
        }
        
        // Store Solana wallet
        if let solanaWallet = solanaWallet {
            biometricHelper.storePrivateKey(solanaWallet.privateKey, for: .solana) { [weak self] success in
                if success {
                    self?.storePublicKey(solanaWallet.publicAddress, for: .solana)
                }
                storedCount += 1
                if storedCount == totalWallets {
                    completion(success)
                }
            }
        }
    }
    
    func getPrivateKey(for blockchain: BlockchainType, completion: @escaping (String?) -> Void) {
        biometricHelper.getPrivateKey(for: blockchain, completion: completion)
    }
    
    func getPublicKey(for blockchain: BlockchainType) -> String? {
        return publicKeyStorage.getToken() // Using existing secure storage method
    }
    
    func hasWallet(for blockchain: BlockchainType) -> Bool {
        return biometricHelper.hasPrivateKey(for: blockchain)
    }
    
    func clearAllWallets(completion: @escaping (Bool) -> Void) {
        var clearedCount = 0
        let totalBlockchains = BlockchainType.allCases.count
        
        for blockchain in BlockchainType.allCases {
            biometricHelper.deletePrivateKey(for: blockchain) { [weak self] success in
                if success {
                    self?.deletePublicKey(for: blockchain)
                }
                clearedCount += 1
                if clearedCount == totalBlockchains {
                    completion(true)
                }
            }
        }
    }
    
    // MARK: - Public Key Storage (Non-biometric)
    private func storePublicKey(_ publicKey: String, for blockchain: BlockchainType) {
        let key = "\(blockchain.rawValue)_public_key"
        UserDefaults.standard.set(publicKey, forKey: key)
    }
    
    private func deletePublicKey(for blockchain: BlockchainType) {
        let key = "\(blockchain.rawValue)_public_key"
        UserDefaults.standard.removeObject(forKey: key)
    }
    
    func getPublicAddress(for blockchain: BlockchainType) -> String? {
        let key = "\(blockchain.rawValue)_public_key"
        return UserDefaults.standard.string(forKey: key)
    }
    
    // MARK: - Biometric Availability
    func isBiometricAvailable() -> Bool {
        return BiometricHelper.isBiometricAvailable()
    }
    
    func isHardwareSupported() -> Bool {
        return BiometricHelper.isHardwareSupported()
    }
}