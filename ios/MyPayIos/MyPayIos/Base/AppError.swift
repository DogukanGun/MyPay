import Foundation

// MARK: - App Error Protocol
protocol AppErrorProtocol: LocalizedError {
    var title: String { get }
    var code: Int { get }
}

// MARK: - App Error Types
enum AppError: AppErrorProtocol {
    case authentication(AuthenticationError)
    case network(NetworkError)
    case validation(ValidationError)
    case configuration(ConfigurationError)
    case unknown(String)
    
    // MARK: - Title
    var title: String {
        switch self {
        case .authentication:
            return "Authentication Error"
        case .network:
            return "Network Error"
        case .validation:
            return "Validation Error"
        case .configuration:
            return "Configuration Error"
        case .unknown:
            return "Unknown Error"
        }
    }
    
    // MARK: - Error Description
    var errorDescription: String? {
        switch self {
        case .authentication(let error):
            return error.errorDescription
        case .network(let error):
            return error.errorDescription
        case .validation(let error):
            return error.errorDescription
        case .configuration(let error):
            return error.errorDescription
        case .unknown(let message):
            return message
        }
    }
    
    // MARK: - Error Code
    var code: Int {
        switch self {
        case .authentication(let error):
            return error.code
        case .network(let error):
            return error.code
        case .validation(let error):
            return error.code
        case .configuration(let error):
            return error.code
        case .unknown:
            return -1
        }
    }
}

// MARK: - Authentication Errors
enum AuthenticationError: AppErrorProtocol {
    case userCancelled
    case networkError
    case invalidCredentials
    case tokenExpired
    case configurationError
    case firebaseNotInitialized
    case unknown(String)
    
    var title: String { "Authentication Error" }
    
    var errorDescription: String? {
        switch self {
        case .userCancelled:
            return "Authentication was cancelled by user"
        case .networkError:
            return "Network error during authentication"
        case .invalidCredentials:
            return "Invalid credentials provided"
        case .tokenExpired:
            return "Authentication token has expired"
        case .configurationError:
            return "Authentication configuration error"
        case .firebaseNotInitialized:
            return "Firebase is not properly initialized"
        case .unknown(let message):
            return message
        }
    }
    
    var code: Int {
        switch self {
        case .userCancelled: return 1001
        case .networkError: return 1002
        case .invalidCredentials: return 1003
        case .tokenExpired: return 1004
        case .configurationError: return 1005
        case .firebaseNotInitialized: return 1006
        case .unknown: return 1007
        }
    }
}

// MARK: - Network Errors
enum NetworkError: AppErrorProtocol {
    case noConnection
    case timeout
    case serverError(Int)
    case invalidResponse
    case decodingError
    
    var title: String { "Network Error" }
    
    var errorDescription: String? {
        switch self {
        case .noConnection:
            return "No internet connection available"
        case .timeout:
            return "Request timed out"
        case .serverError(let code):
            return "Server error with code: \(code)"
        case .invalidResponse:
            return "Invalid response from server"
        case .decodingError:
            return "Failed to decode server response"
        }
    }
    
    var code: Int {
        switch self {
        case .noConnection: return 2001
        case .timeout: return 2002
        case .serverError(let code): return code
        case .invalidResponse: return 2003
        case .decodingError: return 2004
        }
    }
}

// MARK: - Validation Errors
enum ValidationError: AppErrorProtocol {
    case invalidEmail
    case invalidWalletAddress
    case emptyField(String)
    case invalidFormat(String)
    
    var title: String { "Validation Error" }
    
    var errorDescription: String? {
        switch self {
        case .invalidEmail:
            return "Invalid email format"
        case .invalidWalletAddress:
            return "Invalid wallet address format"
        case .emptyField(let field):
            return "\(field) cannot be empty"
        case .invalidFormat(let field):
            return "Invalid format for \(field)"
        }
    }
    
    var code: Int {
        switch self {
        case .invalidEmail: return 3001
        case .invalidWalletAddress: return 3002
        case .emptyField: return 3003
        case .invalidFormat: return 3004
        }
    }
}

// MARK: - Configuration Errors
enum ConfigurationError: AppErrorProtocol {
    case missingKey(String)
    case invalidConfiguration(String)
    case fileNotFound(String)
    
    var title: String { "Configuration Error" }
    
    var errorDescription: String? {
        switch self {
        case .missingKey(let key):
            return "Missing configuration key: \(key)"
        case .invalidConfiguration(let config):
            return "Invalid configuration: \(config)"
        case .fileNotFound(let file):
            return "Configuration file not found: \(file)"
        }
    }
    
    var code: Int {
        switch self {
        case .missingKey: return 4001
        case .invalidConfiguration: return 4002
        case .fileNotFound: return 4003
        }
    }
} 