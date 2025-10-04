//
//  UserStorage.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import Foundation
import Security

class UserStorage {
    static let shared = UserStorage()
    
    private let service = "com.mypay.user"
    private let displayNameAccount = "display_name"
    private let twitterIdAccount = "twitter_id"
    
    private init() {}
    
    // MARK: - Save Methods
    func saveDisplayName(_ displayName: String?) {
        guard let displayName = displayName else { return }
        saveToKeychain(displayName, account: displayNameAccount)
    }
    
    func saveTwitterId(_ twitterId: String) {
        saveToKeychain(twitterId, account: twitterIdAccount)
    }
    
    // MARK: - Get Methods
    func getDisplayName() -> String? {
        return getFromKeychain(account: displayNameAccount)
    }
    
    func getTwitterId() -> String? {
        return getFromKeychain(account: twitterIdAccount)
    }
    
    // MARK: - Save All User Info
    func saveUserInfo(displayName: String?, twitterId: String?) {
        
        if let displayName = displayName {
            saveDisplayName(displayName)
        }

        if let twitterId = twitterId {
            saveTwitterId(twitterId)
        }
        
    }
    
    // MARK: - Delete Methods
    func deleteDisplayName() {
        deleteFromKeychain(account: displayNameAccount)
    }
    func deleteTwitterId() {
        deleteFromKeychain(account: twitterIdAccount)
    }
    
    func deleteAllUserInfo() {
        deleteDisplayName()
        deleteTwitterId()
    }
    
    // MARK: - Check if user exists
    func hasUserInfo() -> Bool {
        return getTwitterId() != nil
    }
    
    // MARK: - Private Keychain Methods
    private func saveToKeychain(_ value: String, account: String) {
        let valueData = Data(value.utf8)
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecValueData as String: valueData
        ]
        
        // Delete existing value first
        SecItemDelete(query as CFDictionary)
        
        // Add new value
        let status = SecItemAdd(query as CFDictionary, nil)
        
        if status != errSecSuccess {
            print("Failed to save \(account) to Keychain: \(status)")
        }
    }
    
    private func getFromKeychain(account: String) -> String? {
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
            if let data = dataTypeRef as? Data {
                return String(data: data, encoding: .utf8)
            }
        }
        
        return nil
    }
    
    private func deleteFromKeychain(account: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
        
        let status = SecItemDelete(query as CFDictionary)
        
        if status != errSecSuccess && status != errSecItemNotFound {
            print("Failed to delete \(account) from Keychain: \(status)")
        }
    }
}
