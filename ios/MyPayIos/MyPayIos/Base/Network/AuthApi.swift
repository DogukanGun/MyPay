//
//  AuthApi.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import UIKit

enum AuthEndpoint: String {
    case checkIfUserExists = "/api/user/check"
    case register = "/api/user/register"
    case profile = "/api/user/profile"
    case newSession = "/api/user/session"
}

struct CheckIfUserRequest: Codable {
    var device_identifier: String
}

struct CheckIfUserResponse: Codable {
    var register: Bool
    var device_changed: Bool
}

struct RegisterUserRequest: Codable {
    var device_identifier: String
    var twitter_id: String
    var username: String
}

struct NewSessionRequest: Codable {
    var device_identifier: String
}

struct Wallet: Codable {
    var ethWallet: WalletInfo?
    var solanaWallet: WalletInfo?
    
    enum CodingKeys: String, CodingKey {
        case ethWallet = "eth_wallet"
        case solanaWallet = "solana_wallet"
    }
}

struct NewSessionResponse: Codable {
    var uid: String
    var username: String
    var twitter_id: String
    var wallets: Wallet?

    
    enum CodingKeys: String, CodingKey {
        case uid, username, twitter_id, wallets
    }
}

struct WalletInfo: Codable {
    let privateKey: String
    let publicAddress: String
    
    enum CodingKeys: String, CodingKey {
        case privateKey = "private_key"
        case publicAddress = "public_address"
    }
}

struct RegisterUserResponse: Codable {
    var uid: String
    var username: String
    var twitter_id: String
    var ethWallet: WalletInfo?
    var solanaWallet: WalletInfo?
    
    enum CodingKeys: String, CodingKey {
        case uid
        case username
        case twitter_id
        case ethWallet = "eth_wallet"
        case solanaWallet = "solana_wallet"
    }
}

class AuthApi: BaseApi {
    
    static let shared = AuthApi()
    
    private override init() {
        super.init()
    }
    
    private func getDeviceIdentifier() -> String {
        // Use identifierForVendor as primary device identifier
        // This is unique per vendor (app) and persists across app launches
        if let vendorId = UIDevice.current.identifierForVendor?.uuidString {
            return vendorId
        }
        
        // Fallback to device info if vendor ID is nil (very rare)
        let model = UIDevice.current.model.replacingOccurrences(of: " ", with: "_")
        let systemVersion = UIDevice.current.systemVersion.replacingOccurrences(of: ".", with: "_")
        return "fallback_\(model)_\(systemVersion)_\(Int.random(in: 1000...9999))"
    }
    
    // FIXED: Changed from GET to POST with device_identifier
    func checkIfUserExists(completion: @escaping (Result<CheckIfUserResponse, AppError>) -> Void) {
        let request = CheckIfUserRequest(device_identifier: getDeviceIdentifier())
        performPostRequest<CheckIfUserRequest, CheckIfUserResponse>(
            endpoint: AuthEndpoint.checkIfUserExists.rawValue,
            requestBody: request
        ) { result in
            completion(result)
        }
    }
    
    // FIXED: Added device_identifier to request
    func registerUser(twitterId: String, username: String, completion: @escaping (Result<RegisterUserResponse, AppError>) -> Void) {
        let request = RegisterUserRequest(
            device_identifier: getDeviceIdentifier(),
            twitter_id: twitterId, 
            username: username
        )
        performPostRequest<RegisterUserRequest, RegisterUserResponse>(
            endpoint: AuthEndpoint.register.rawValue,
            requestBody: request
        ) { result in
            completion(result)
        }
    }
    
    // NEW: Added newSession endpoint for device changes
    func newSession(completion: @escaping (Result<NewSessionResponse, AppError>) -> Void) {
        let request = NewSessionRequest(device_identifier: getDeviceIdentifier())
        performPostRequest<NewSessionRequest, NewSessionResponse>(
            endpoint: AuthEndpoint.newSession.rawValue,
            requestBody: request
        ) { result in
            completion(result)
        }
    }
}
