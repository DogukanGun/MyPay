//
//  JarvisApi.swift
//  MyPayIos
//
//  Created by AI Assistant on 16.10.25.
//

import Foundation

enum JarvisEndpoint: String {
    case ask = "/api/agent/ask"
}

struct JarvisAskRequest: Codable {
    var input: String
    var mentioned_ids: [String]
    var reply_to: String?
}

struct JarvisAskResponse: Codable {
    var output: String
}

struct JarvisErrorResponse: Codable {
    var error: String
}

class JarvisApi: BaseApi {
    
    static let shared = JarvisApi()
    
    private override init() {
        super.init()
    }
    
    func ask(input: String, completion: @escaping (Result<JarvisAskResponse, AppError>) -> Void) {
        let request = JarvisAskRequest(
            input: input,
            mentioned_ids: [],
            reply_to: nil
        )
        
        // Debug logging
        print("DEBUG: Sending Jarvis request")
        print("DEBUG: Input: \(input)")
        print("DEBUG: Mentioned IDs: []")
        print("DEBUG: Reply To: nil")
        
        // Check if token exists
        if let token = TokenStorage.shared.getToken() {
            let tokenPreview = String(token.prefix(20))
            print("DEBUG: Token exists - starts with: \(tokenPreview)...")
        } else {
            print("DEBUG: WARNING - No token found in TokenStorage!")
        }
        
        performPostRequest(
            endpoint: JarvisEndpoint.ask.rawValue,
            requestBody: request
        ) { (result: Result<JarvisAskResponse, AppError>) in
            switch result {
            case .success(let response):
                print("DEBUG: Jarvis success - output: \(response.output)")
            case .failure(let error):
                print("DEBUG: Jarvis error - \(error.errorDescription ?? "Unknown error")")
            }
            completion(result)
        }
    }
}

