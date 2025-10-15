//
//  HomeViewModel.swift
//  MyPayIos
//
//  Created by Claude Code on 15.09.25.
//

import Foundation
import Combine

// MARK: - Home View Model
final class HomeViewModel: BaseViewModel {
    
    // MARK: - Published Properties
    @Published private(set) var userProfile: UserProfile?
    @Published private(set) var walletBalance: WalletBalance?
    @Published private(set) var portfolioItems: [PortfolioItem] = []
    @Published private(set) var isRefreshing: Bool = false
    @Published var selectedChain: BlockchainChain = .solana
    
    // Payment Bottom Sheet Properties
    @Published var showPaymentSheet: Bool = false
    @Published var isSendMode: Bool = true
    @Published var paymentQRCodeURL: String? = nil
    
    // MARK: - Computed Properties
    var displayName: String {
        userProfile?.displayName ?? userProfile?.email ?? "Unknown User"
    }
    
    var formattedWalletAddress: String {
        guard let userProfile = userProfile else { return "" }
        switch selectedChain {
        case .ethereum:
            return userProfile.ethWalletAddress?.formatWalletAddress() ?? ""
        case .solana:
            return userProfile.solanaWalletAddress.formatWalletAddress()
        }
    }
    
    var formattedBalance: String {
        switch selectedChain {
        case .ethereum:
            return walletBalance?.formattedEthBalance ?? "0.00 ETH"
        case .solana:
            return walletBalance?.formattedSolanaBalance ?? "0.00 SOL"
        }
    }
    
    var verifierName: String {
        userProfile?.verifier ?? "Unknown"
    }
    
    // MARK: - Initialization
    override init() {
        super.init()
        setupInitialData()
        setupNotificationObservers()
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    // MARK: - Public Methods
    func loadUserData() {
        executeAsync(
            operation: { [weak self] in
                try await self?.fetchUserData()
            },
            onSuccess: { [weak self] _ in
                self?.setupPortfolioItems()
            }
        )
    }
    
    func refreshData() {
        guard !isRefreshing else { return }
        
        isRefreshing = true
        
        executeAsync(
            operation: { [weak self] in
                try await self?.fetchUserData()
            },
            onSuccess: { [weak self] _ in
                self?.setupPortfolioItems()
                self?.isRefreshing = false
            },
            onError: { [weak self] _ in
                self?.isRefreshing = false
            }
        )
    }
    
    func handlePayAction() {
        isSendMode = true
        showPaymentSheet = true
    }
    
    func handleReceiveAction() {
        isSendMode = false
        showPaymentSheet = true
    }
    
    func dismissPaymentSheet() {
        showPaymentSheet = false
        paymentQRCodeURL = nil
    }
    
    func initiateQRPayment(amount: Double, publicKey: String) {
        let transferFields = TransferRequestURLFields(
            recipient: publicKey,
            amount: Decimal(amount),
            tokenDecimal: selectedChain == .solana ? 9 : 18,
            splToken: selectedChain == .solana ? nil : "USDC_TOKEN_ADDRESS" // Replace with actual USDC token address
        )
        
        // Debug the input data
        print("DEBUG: Creating QR with amount: \(amount), publicKey: \(publicKey)")
        print("DEBUG: TransferFields - recipient: \(transferFields.recipient), amount: \(transferFields.amount)")
        
        // Create Solana Pay URL for QR code
        SolanaHelper.shared.prepareSolanaPay(transferRequestField: transferFields) { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(let url):
                    print("DEBUG: Successfully created Solana Pay URL: \(url.absoluteString)")
                    // Store the URL for QR code generation - this will trigger the UI to show the QR code
                    self?.paymentQRCodeURL = url.absoluteString
                case .failure(let error):
                    print("DEBUG: Failed to create Solana Pay URL: \(error)")
                    print("DEBUG: Error details: \(String(describing: error))")
                    // Handle error appropriately - dismiss sheet on error
                    self?.dismissPaymentSheet()
                }
            }
        }
        
        // Don't dismiss the sheet here - let it stay open to show the QR code
    }
    
    func openJarvis() {
        navigate(to: .jarvis)
    }
    
    func openSettings() {
        navigate(to: .settings)
    }
    
    func logout() {
        // Clear all user data
        UserStorage.shared.deleteAllUserInfo()
        TokenStorage.shared.deleteToken()
        
        // Clear wallet data
        WalletManager.shared.clearAllWallets { _ in
            // Navigation handled below
        }
        
        clearUserData()
        reset(to: .login)
    }
    
    func switchChain(_ chain: BlockchainChain) {
        selectedChain = chain
        loadUserData()
    }
    
    // MARK: - Private Methods
    private func setupInitialData() {
        userProfile = nil
        walletBalance = nil
        portfolioItems = []
    }
    
    private func setupNotificationObservers() {
        // No longer needed since we removed NFC functionality
        // QR code payments are handled directly through UI interactions
    }
    
    private func fetchUserData() async throws {
        let walletManager = WalletManager.shared
        
        let solanaAddress = walletManager.getPublicAddress(for: .solana)
        let ethAddress = walletManager.getPublicAddress(for: .ethereum)
        
        guard let solanaAddress = solanaAddress else {
            // TODO: send wallet create request or kill the session
            return
        }
        
        let balance: Decimal
        switch selectedChain {
        case .solana:
            let solanaBalance = try await getSolanaBalance(for: solanaAddress)
            balance = Decimal(solanaBalance) / 1_000_000_000 // Convert lamports to SOL
        case .ethereum:
            guard let ethAddress = ethAddress else {
                throw AppError.network(.invalidResponse)
            }
            let ethBalance = try await getEthereumBalance(for: ethAddress)
            balance = ethBalance
        }
        
        let profileData = UserProfile(
            id: UserStorage.shared.getTwitterId() ?? "",
            email: UserStorage.shared.getDisplayName() ?? "",
            solanaWalletAddress: solanaAddress,
            ethWalletAddress: ethAddress,
            verifier: "Nexarb"
        )
        
        await MainActor.run {
            self.userProfile = profileData
            switch self.selectedChain {
            case .solana:
                self.walletBalance = WalletBalance(solanaBalance: balance)
            case .ethereum:
                self.walletBalance = WalletBalance(ethBalance: balance)
            }
        }
    }
    
    private func getSolanaBalance(for publicKey: String) async throws -> UInt64 {
        #if DEBUG
        let rpcUrl = "https://api.devnet.solana.com"
        #else
        let rpcUrl = "https://api.mainnet-beta.solana.com"
        #endif
        
        guard let url = URL(string: rpcUrl) else {
            throw AppError.network(.invalidResponse)
        }
        
        let requestBody = [
            "jsonrpc": "2.0",
            "id": 1,
            "method": "getBalance",
            "params": [publicKey]
        ] as [String: Any]
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AppError.network(.invalidResponse)
        }
        
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let result = json["result"] as? [String: Any],
              let value = result["value"] as? UInt64 else {
            throw AppError.network(.invalidResponse)
        }
        
        return value
    }
    
    private func getEthereumBalance(for address: String) async throws -> Decimal {
        #if DEBUG
        let rpcUrl = "https://ethereum-sepolia.publicnode.com"
        #else
        let rpcUrl = "https://ethereum.publicnode.com"
        #endif
        
        guard let url = URL(string: rpcUrl) else {
            throw AppError.network(.invalidResponse)
        }
        
        let requestBody = [
            "jsonrpc": "2.0",
            "id": 1,
            "method": "eth_getBalance",
            "params": [address, "latest"]
        ] as [String: Any]
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AppError.network(.invalidResponse)
        }
        
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let result = json["result"] as? String else {
            throw AppError.network(.invalidResponse)
        }
        
        // Convert hex string to decimal (Wei) then to ETH
        let hexString = String(result.dropFirst(2)) // Remove "0x" prefix
        guard let weiValue = UInt64(hexString, radix: 16) else {
            throw AppError.network(.invalidResponse)
        }
        
        return Decimal(weiValue) / Decimal(pow(10.0, 18.0)) // Convert Wei to ETH
    }
    
    private func setupPortfolioItems() {
        guard let balance = walletBalance else { return }
        
        switch selectedChain {
        case .solana:
            let solanaItem = PortfolioItem(
                symbol: "SOL",
                name: "Solana",
                balance: balance.solanaBalance,
                iconColor: "primary"
            )
            
            let usdcItem = PortfolioItem(
                symbol: "USDC",
                name: "USD Coin",
                balance: balance.usdcBalance,
                iconColor: "secondary"
            )
            
            portfolioItems = [solanaItem, usdcItem]
            
        case .ethereum:
            let ethItem = PortfolioItem(
                symbol: "ETH",
                name: "Ethereum",
                balance: balance.ethBalance,
                iconColor: "primary"
            )
            
            let usdcItem = PortfolioItem(
                symbol: "USDC",
                name: "USD Coin",
                balance: balance.usdcBalance,
                iconColor: "secondary"
            )
            
            portfolioItems = [ethItem, usdcItem]
        }
    }
    
    private func clearUserData() {
        userProfile = nil
        walletBalance = nil
        portfolioItems = []
    }
}
