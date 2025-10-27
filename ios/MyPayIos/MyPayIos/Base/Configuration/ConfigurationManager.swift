import Foundation

// MARK: - Configuration Manager Protocol
protocol ConfigurationManagerProtocol {
    var supabaseURL: URL { get throws }
    var supabaseKey: String { get throws }
    var environment: AppEnvironment { get }
    var isDebugMode: Bool { get }
}

// MARK: - App Environment
enum AppEnvironment: String, CaseIterable {
    case development = "Development"
    case staging = "Staging"
    case production = "Production"
    
    var displayName: String {
        return rawValue
    }
    
    var isProduction: Bool {
        return self == .production
    }
}

// MARK: - Configuration Manager Implementation
final class ConfigurationManager: ConfigurationManagerProtocol {
    
    // MARK: - Singleton
    static let shared = ConfigurationManager()
    
    private init() {}
    
    // MARK: - Environment Detection
    var environment: AppEnvironment {
        #if DEBUG
        return .development
        #elseif STAGING
        return .staging
        #else
        return .production
        #endif
    }
    
    var isDebugMode: Bool {
        #if DEBUG
        return true
        #else
        return false
        #endif
    }
    
    // MARK: - Supabase Configuration
    var supabaseURL: URL {
        get throws {
            guard let urlString = Bundle.main.object(forInfoDictionaryKey: "SUPABASE_URL") as? String,
                  !urlString.isEmpty else {
                throw AppError.configuration(.missingKey("SUPABASE_URL"))
            }
            
            let fullURLString = urlString.hasPrefix("http") ? urlString : "https://\(urlString)"
            
            guard let url = URL(string: fullURLString) else {
                throw AppError.configuration(.invalidConfiguration("Invalid SUPABASE_URL format: \(urlString)"))
            }
            
            return url
        }
    }
    
    var supabaseKey: String {
        get throws {
            guard let key = Bundle.main.object(forInfoDictionaryKey: "SUPABASE_KEY") as? String,
                  !key.isEmpty else {
                throw AppError.configuration(.missingKey("SUPABASE_KEY"))
            }
            
            return key
        }
    }
    
    // MARK: - App Information
    var appVersion: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "Unknown"
    }
    
    var buildNumber: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "Unknown"
    }
    
    var bundleIdentifier: String {
        Bundle.main.bundleIdentifier ?? "Unknown"
    }
    
    // MARK: - Validation
    func validateConfiguration() throws {
        // Validate required configuration keys
        _ = try supabaseURL
        _ = try supabaseKey
        
        print("Configuration validated successfully")
        print("App: \(appVersion) (\(buildNumber))")
        print("Bundle ID: \(bundleIdentifier)")
        print("Environment: \(environment.displayName)")
        print("Debug Mode: \(isDebugMode)")
    }
    
    // MARK: - Helper Methods
    func getValue(for key: String) -> Any? {
        return Bundle.main.object(forInfoDictionaryKey: key)
    }
    
    func getStringValue(for key: String) throws -> String {
        guard let value = Bundle.main.object(forInfoDictionaryKey: key) as? String,
              !value.isEmpty else {
            throw AppError.configuration(.missingKey(key))
        }
        return value
    }
} 
