//
//  ApiHelper.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 21.09.25.
//

import Foundation
import Alamofire

open class BaseApi {
        
    private let session: Session
    private let configManager = ConfigurationManager.shared
    
    init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        self.session = Session(configuration: configuration)
    }
    
    private var baseURL: String {
        switch configManager.environment {
        case .development:
            return "https://api.nexarb.com"
        case .staging:
            return "https://api.nexarb.com"
        case .production:
            return "https://api.nexarb.com"
        }
    }
    
    func performPostRequest<T: Codable, R: Codable>(
        endpoint: String,
        requestBody: T,
        completion: @escaping (Result<R, AppError>) -> Void
    ) {
        let url = baseURL + endpoint
        
        session.request(
            url,
            method: .post,
            parameters: requestBody,
            encoder: JSONParameterEncoder.default,
            headers: getHeaders()
        )
        .validate(statusCode: 200..<300)
        .responseData { response in
            switch response.result {
            case .success(let data):
                do {
                    let decodedResponse = try JSONDecoder().decode(R.self, from: data)
                    completion(.success(decodedResponse))
                } catch {
                    completion(.failure(AppError.network(.decodingError)))
                }
            case .failure(let error):
                let appError = self.mapAlamofireError(error)
                completion(.failure(appError))
            }
        }
    }
    
    func performGetRequest<R: Codable>(
        endpoint: String,
        completion: @escaping (Result<R, AppError>) -> Void
    ) {
        let url = baseURL + endpoint
        
        session.request(
            url,
            method: .get,
            headers: getHeaders()
        )
        .validate(statusCode: 200..<300)
        .responseData { response in
            switch response.result {
            case .success(let data):
                do {
                    let decodedResponse = try JSONDecoder().decode(R.self, from: data)
                    completion(.success(decodedResponse))
                } catch {
                    completion(.failure(AppError.network(.decodingError)))
                }
            case .failure(let error):
                let appError = self.mapAlamofireError(error)
                completion(.failure(appError))
            }
        }
    }
    
    private func post<T: Codable, R: Codable>(
        endpoint: String,
        requestBody: T,
        completion: @escaping (Result<R, AppError>) -> Void
    ) {
        performPostRequest(
            endpoint: endpoint,
            requestBody: requestBody,
            completion: completion
        )
    }
    
    private func get<R: Codable>(
        endpoint: String,
        completion: @escaping (Result<R, AppError>) -> Void
    ) {
        performGetRequest(
            endpoint: endpoint,
            completion: completion
        )
    }
    
    private func getHeaders() -> HTTPHeaders {
        var headers: HTTPHeaders = [
            "Content-Type": "application/json",
            "Accept": "application/json"
        ]
        
        // Add Authorization header if token exists
        if let token = TokenStorage.shared.getToken() {
            headers["Authorization"] = "Bearer \(token)"
        }
        
        return headers
    }
    
    private func mapAlamofireError(_ error: AFError) -> AppError {
        switch error {
        case .sessionTaskFailed(let sessionError):
            if let urlError = sessionError as? URLError {
                switch urlError.code {
                case .notConnectedToInternet, .networkConnectionLost:
                    return AppError.network(.noConnection)
                case .timedOut:
                    return AppError.network(.timeout)
                default:
                    return AppError.network(.serverError(urlError.errorCode))
                }
            }
            return AppError.network(.serverError(-1))
        case .responseValidationFailed(let reason):
            switch reason {
            case .unacceptableStatusCode(let code):
                return AppError.network(.serverError(code))
            default:
                return AppError.network(.invalidResponse)
            }
        default:
            return AppError.network(.invalidResponse)
        }
    }
}

