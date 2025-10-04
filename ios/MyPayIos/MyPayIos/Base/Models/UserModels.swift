import Foundation

// MARK: - User Profile Model
struct UserProfile: Codable, Equatable {
    let id: String
    let email: String
    let displayName: String?
    let solanaWalletAddress: String
    let ethWalletAddress: String?
    let verifier: String
    let createdAt: Date
    let updatedAt: Date
    
    init(
        id: String,
        email: String,
        displayName: String? = nil,
        solanaWalletAddress: String,
        ethWalletAddress: String? = nil,
        verifier: String,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.email = email
        self.displayName = displayName
        self.solanaWalletAddress = solanaWalletAddress
        self.ethWalletAddress = ethWalletAddress
        self.verifier = verifier
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}

// MARK: - Wallet Balance Model
struct WalletBalance: Codable, Equatable {
    let solanaBalance: Decimal
    let ethBalance: Decimal
    let usdcBalance: Decimal
    let totalUSDValue: Decimal?
    let lastUpdated: Date
    
    init(
        solanaBalance: Decimal = 0,
        ethBalance: Decimal = 0,
        usdcBalance: Decimal = 0,
        totalUSDValue: Decimal? = nil,
        lastUpdated: Date = Date()
    ) {
        self.solanaBalance = solanaBalance
        self.usdcBalance = usdcBalance
        self.totalUSDValue = totalUSDValue
        self.lastUpdated = lastUpdated
        self.ethBalance = ethBalance
    }
    
    // MARK: - Computed Properties
    var formattedSolanaBalance: String {
        return String(format: "%.9f SOL", NSDecimalNumber(decimal: solanaBalance).doubleValue)
    }
    
    var formattedEthBalance: String {
        return String(format: "%.9f ETH", NSDecimalNumber(decimal: ethBalance).doubleValue)
    }
    
    var formattedUSDCBalance: String {
        return String(format: "%.2f USDC", NSDecimalNumber(decimal: usdcBalance).doubleValue)
    }
    
    var formattedTotalUSD: String? {
        guard let totalUSDValue = totalUSDValue else { return nil }
        return String(format: "$%.2f", NSDecimalNumber(decimal: totalUSDValue).doubleValue)
    }
}

// MARK: - Portfolio Item Model
struct PortfolioItem: Codable, Equatable, Identifiable {
    let id: String
    let symbol: String
    let name: String
    let balance: Decimal
    let usdValue: Decimal?
    let iconColor: String
    
    init(
        id: String = UUID().uuidString,
        symbol: String,
        name: String,
        balance: Decimal,
        usdValue: Decimal? = nil,
        iconColor: String
    ) {
        self.id = id
        self.symbol = symbol
        self.name = name
        self.balance = balance
        self.usdValue = usdValue
        self.iconColor = iconColor
    }
    
    // MARK: - Computed Properties
    var formattedBalance: String {
        return String(format: "%.9f %@", NSDecimalNumber(decimal: balance).doubleValue, symbol)
    }
    
    var formattedUSDValue: String? {
        guard let usdValue = usdValue else { return nil }
        return String(format: "$%.2f", NSDecimalNumber(decimal: usdValue).doubleValue)
    }
}

// MARK: - Authentication State Model
enum AuthenticationState {
    case unauthenticated
    case authenticating
    case authenticated(UserProfile)
    case error(AppError)
    
    var isAuthenticated: Bool {
        switch self {
        case .authenticated:
            return true
        default:
            return false
        }
    }
    
    var userProfile: UserProfile? {
        switch self {
        case .authenticated(let profile):
            return profile
        default:
            return nil
        }
    }
}

// MARK: - Loading State Model
enum LoadingState {
    case idle
    case loading
    case success
    case error(AppError)
    
    var isLoading: Bool {
        switch self {
        case .loading:
            return true
        default:
            return false
        }
    }
    
    var hasError: Bool {
        switch self {
        case .error:
            return true
        default:
            return false
        }
    }
}

// MARK: - Utility Extensions
extension String {
    func formatWalletAddress(prefixLength: Int = 8, suffixLength: Int = 8) -> String {
        guard self.count > prefixLength + suffixLength + 3 else { return self }
        let prefix = String(self.prefix(prefixLength))
        let suffix = String(self.suffix(suffixLength))
        return "\(prefix)...\(suffix)"
    }
} 
