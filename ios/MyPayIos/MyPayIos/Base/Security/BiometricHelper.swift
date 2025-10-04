//
//  BiometricHelper.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import Foundation
import LocalAuthentication
import Security
import CryptoKit

class BiometricHelper {
    static let shared = BiometricHelper()
    
    private let context = LAContext()
    private let keyService = "com.mypay.wallet.keys"
    
    private init() {}
    
    // MARK: - Biometric Availability
    static func isBiometricAvailable() -> Bool {
        let context = LAContext()
        var error: NSError?
        
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
    }
    
    static func isHardwareSupported() -> Bool {
        let context = LAContext()
        var error: NSError?
        
        let canEvaluate = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        
        if let error = error {
            // Check if error is due to no biometry enrolled vs no hardware
            return error.code != LAError.biometryNotAvailable.rawValue
        }
        
        return canEvaluate
    }
    
    // MARK: - Key Storage
    func storePrivateKey(_ key: String, for blockchain: BlockchainType, completion: @escaping (Bool) -> Void) {
        let keyData = Data(key.utf8)
        let account = "\(blockchain.rawValue)_private_key"
        
        // First, authenticate with biometrics
        authenticateUser(reason: "Authenticate to secure your \(blockchain.displayName) wallet") { [weak self] success in
            if success {
                self?.saveToKeychain(data: keyData, service: self?.keyService ?? "", account: account) { saved in
                    completion(saved)
                }
            } else {
                completion(false)
            }
        }
    }
    
    func getPrivateKey(for blockchain: BlockchainType, completion: @escaping (String?) -> Void) {
        let account = "\(blockchain.rawValue)_private_key"
        
        // Authenticate before retrieving
        authenticateUser(reason: "Authenticate to access your \(blockchain.displayName) wallet") { [weak self] success in
            if success {
                self?.getFromKeychain(service: self?.keyService ?? "", account: account) { data in
                    if let data = data {
                        completion(String(data: data, encoding: .utf8))
                    } else {
                        completion(nil)
                    }
                }
            } else {
                completion(nil)
            }
        }
    }
    
    func deletePrivateKey(for blockchain: BlockchainType, completion: @escaping (Bool) -> Void) {
        let account = "\(blockchain.rawValue)_private_key"
        
        authenticateUser(reason: "Authenticate to delete your \(blockchain.displayName) wallet") { [weak self] success in
            if success {
                self?.deleteFromKeychain(service: self?.keyService ?? "", account: account) { deleted in
                    completion(deleted)
                }
            } else {
                completion(false)
            }
        }
    }
    
    func hasPrivateKey(for blockchain: BlockchainType) -> Bool {
        let account = "\(blockchain.rawValue)_private_key"
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: keyService,
            kSecAttrAccount as String: account,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        let status = SecItemCopyMatching(query as CFDictionary, nil)
        return status == errSecSuccess
    }
    
    // MARK: - Authentication
    private func authenticateUser(reason: String, completion: @escaping (Bool) -> Void) {
        let context = LAContext()
        context.localizedFallbackTitle = "Use Passcode"
        
        context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, error in
            DispatchQueue.main.async {
                completion(success)
            }
        }
    }
    
    // MARK: - Keychain Operations
    private func saveToKeychain(data: Data, service: String, account: String, completion: @escaping (Bool) -> Void) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
            kSecValueData as String: data
        ]
        
        // Delete existing item first
        SecItemDelete(query as CFDictionary)
        
        // Add new item
        let status = SecItemAdd(query as CFDictionary, nil)
        completion(status == errSecSuccess)
    }
    
    private func getFromKeychain(service: String, account: String, completion: @escaping (Data?) -> Void) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var dataTypeRef: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &dataTypeRef)
        
        if status == errSecSuccess {
            completion(dataTypeRef as? Data)
        } else {
            completion(nil)
        }
    }
    
    private func deleteFromKeychain(service: String, account: String, completion: @escaping (Bool) -> Void) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
        
        let status = SecItemDelete(query as CFDictionary)
        completion(status == errSecSuccess || status == errSecItemNotFound)
    }
}

// MARK: - Blockchain Types
enum BlockchainType: String, CaseIterable {
    case ethereum = "eth"
    case solana = "solana"
    
    var displayName: String {
        switch self {
        case .ethereum:
            return "Ethereum"
        case .solana:
            return "Solana"
        }
    }
}
