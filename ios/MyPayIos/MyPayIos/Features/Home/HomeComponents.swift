//
//  HomeComponents.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 18.09.25.
//

import SwiftUI

// MARK: - Balance Card Component
struct BalanceCard: View {
    let balance: String
    let walletAddress: String
    let email: String
    let isRefreshing: Bool
    let onRefresh: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppConstants.UI.padding) {
            // Header with refresh button
            HStack {
                Text(AppConstants.Strings.availableBalance)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(AppConstants.Colors.textPrimary)
                
                Spacer()
                
                RefreshButton(
                    isLoading: isRefreshing,
                    action: onRefresh
                )
            }
            
            // Loading indicator
            LoadingIndicator(isLoading: isRefreshing)
            
            // Balance display
            Text(balance)
                .font(.system(size: 32, weight: .bold))
                .foregroundColor(AppConstants.Colors.textPrimary)
            
            // Account info
            AccountInfoView(
                walletAddress: walletAddress,
                email: email
            )
        }
        .padding(AppConstants.UI.largePadding)
        .cornerRadius(0)
    }
}

// MARK: - Refresh Button Component
private struct RefreshButton: View {
    let isLoading: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Image(systemName: "arrow.clockwise")
                .foregroundColor(AppConstants.Colors.textPrimary)
                .font(.system(size: 16))
                .rotationEffect(.degrees(isLoading ? 360 : 0))
                .animation(
                    .linear(duration: 1.0).repeatWhileActive(repeating: isLoading),
                    value: isLoading
                )
        }
        .disabled(isLoading)
    }
}

// MARK: - Loading Indicator Component
private struct LoadingIndicator: View {
    let isLoading: Bool
    
    var body: some View {
        if isLoading {
            ProgressView()
                .progressViewStyle(LinearProgressViewStyle(tint: AppConstants.Colors.primary))
                .frame(height: 4)
        } else {
            RoundedRectangle(cornerRadius: 2)
                .fill(
                    LinearGradient(
                        colors: [AppConstants.Colors.primary, AppConstants.Colors.textSecondary.opacity(0.3)],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .frame(height: 4)
        }
    }
}

// MARK: - Account Info View Component
private struct AccountInfoView: View {
    let walletAddress: String
    let email: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(walletAddress.formatWalletAddress())
                .font(.system(size: 14))
                .foregroundColor(AppConstants.Colors.textSecondary)
            
            Text(email)
                .font(.system(size: 14))
                .foregroundColor(AppConstants.Colors.textSecondary)
        }
    }
}

// MARK: - Animation Extension
extension Animation {
    func repeatWhileActive(repeating: Bool) -> Animation {
        repeating ? self.repeatForever(autoreverses: false) : self
    }
}

// MARK: - Action Buttons Component
struct ActionButtons: View {
    let onPayTapped: () -> Void
    let onReceiveTapped: () -> Void
    
    var body: some View {
        HStack(spacing: AppConstants.UI.padding) {
            ActionButton(
                title: AppConstants.Strings.pay,
                icon: "arrow.up.right",
                backgroundColor: AppConstants.Colors.primary,
                foregroundColor: AppConstants.Colors.textPrimary,
                action: onPayTapped
            )
            
            ActionButton(
                title: AppConstants.Strings.receive,
                icon: "arrow.down.left",
                backgroundColor: AppConstants.Colors.secondary,
                foregroundColor: .white,
                action: onReceiveTapped
            )
        }
        .padding(.horizontal, AppConstants.UI.largePadding)
    }
}

// MARK: - Action Button Component
private struct ActionButton: View {
    let title: String
    let icon: String
    let backgroundColor: Color
    let foregroundColor: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 16, weight: .semibold))
                Text(title)
                    .font(.system(size: 16, weight: .semibold))
            }
            .foregroundColor(foregroundColor)
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .background(backgroundColor)
            .cornerRadius(AppConstants.UI.buttonCornerRadius)
        }
    }
}

// MARK: - Portfolio Section Component
struct PortfolioSection: View {
    let portfolioItems: [PortfolioItem]
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppConstants.UI.padding) {
            SectionHeader(title: AppConstants.Strings.portfolio)
            
            HStack(spacing: AppConstants.UI.padding) {
                ForEach(portfolioItems) { item in
                    PortfolioCard(portfolioItem: item)
                }
            }
            .padding(.horizontal, AppConstants.UI.largePadding)
        }
        .shadow(radius: 2) 
    }
}

// MARK: - Section Header Component
private struct SectionHeader: View {
    let title: String
    
    var body: some View {
        HStack {
            Text(title)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(AppConstants.Colors.textPrimary)
            Spacer()
        }
        .padding(.horizontal, AppConstants.UI.largePadding)
    }
}

// MARK: - Portfolio Card Component
struct PortfolioCard: View {
    let portfolioItem: PortfolioItem
    
    var iconColor: Color = AppConstants.Colors.primary
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Circle()
                    .fill(iconColor)
                    .frame(width: 24, height: 24)
                
                Text(portfolioItem.name)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(AppConstants.Colors.textPrimary)
                
                Spacer()
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(portfolioItem.formattedBalance)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(AppConstants.Colors.textPrimary)
                
                if let usdValue = portfolioItem.formattedUSDValue {
                    Text(usdValue)
                        .font(.system(size: 14))
                        .foregroundColor(AppConstants.Colors.textSecondary)
                }
            }
        }
        .padding(AppConstants.UI.padding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .cornerRadius(AppConstants.UI.cornerRadius)
    }
}

// MARK: - Account Information Section Component
struct AccountInformationSection: View {
    let userProfile: UserProfile?
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppConstants.UI.padding) {
            SectionHeader(title: AppConstants.Strings.accountInformation)
            
            if let profile = userProfile {
                VStack(alignment: .leading, spacing: 12) {
                    AccountInfoRow(
                        label: AppConstants.Strings.name,
                        value: profile.displayName ?? profile.email
                    )
                    AccountInfoRow(
                        label: AppConstants.Strings.wallet,
                        value: profile.solanaWalletAddress.formatWalletAddress(prefixLength: 16, suffixLength: 16)
                    )
                    AccountInfoRow(
                        label: AppConstants.Strings.verifier,
                        value: profile.verifier
                    )
                }
                .padding(AppConstants.UI.padding)
                .background(AppConstants.Colors.background)
                .cornerRadius(AppConstants.UI.cornerRadius)
                .padding(.horizontal, AppConstants.UI.largePadding)
            } else {
                EmptyAccountView()
            }
        }
    }
}

// MARK: - Empty Account View Component
private struct EmptyAccountView: View {
    var body: some View {
        VStack {
            Image(systemName: "person.circle")
                .font(.system(size: 48))
                .foregroundColor(AppConstants.Colors.textSecondary)
            
            Text("Account information not available")
                .font(.system(size: 16))
                .foregroundColor(AppConstants.Colors.textSecondary)
        }
        .frame(maxWidth: .infinity, minHeight: 100)
        .background(AppConstants.Colors.background)
        .cornerRadius(AppConstants.UI.cornerRadius)
        .padding(.horizontal, AppConstants.UI.largePadding)
    }
}

// MARK: - Account Info Row Component
private struct AccountInfoRow: View {
    let label: String
    let value: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(AppConstants.Colors.textPrimary)
            
            Text(value)
                .font(.system(size: 14))
                .foregroundColor(AppConstants.Colors.textSecondary)
                .lineLimit(nil)
                .textSelection(.enabled) // Allow text selection for copying
        }
    }
}

// MARK: - Custom Bottom Navigation Bar
struct CustomBottomNavBar: View {
    @Binding var selectedTab: Int
    @ObservedObject var scrollStateManager: ScrollStateManager
    @StateObject private var messageManager = BottomNavMessageManager.shared
    @State private var isManuallyCollapsed: Bool = false

    
    private var shouldShowFullNav: Bool {
        // Hide nav buttons when showing message, otherwise show based on scroll state
        if messageManager.showMessage || messageManager.shouldDelayScroll {
            return false
        }
        return (!scrollStateManager.isScrolling || scrollStateManager.isScrollingUp) && !isManuallyCollapsed
    }
    
    private var shouldShowMessage: Bool {
        messageManager.showMessage
    }
    
    private var navBarHeight: CGFloat {
        if shouldShowMessage {
            return AppConstants.UI.bottomNavHeight
        }
        return shouldShowFullNav ? AppConstants.UI.bottomNavHeight : AppConstants.UI.bottomNavCollapsedHeight
    }
    
    private var navBarWidth: CGFloat {
        if shouldShowMessage {
            return UIScreen.main.bounds.width - 32
        }
        return shouldShowFullNav ? UIScreen.main.bounds.width - 32 : AppConstants.UI.bottomNavCollapsedWidth
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Message Display
            if shouldShowMessage {
                MessageDisplayView(message: messageManager.currentMessage)
                    .transition(.asymmetric(
                        insertion: .scale(scale: 0.9).combined(with: .opacity),
                        removal: .scale(scale: 1.1).combined(with: .opacity)
                    ))
            }
            
            // Navigation Items with smooth animation
            if shouldShowFullNav {
                HStack {
                    Spacer()
                    
                    // Home Tab
                    BottomNavItem(
                        icon: "house.fill",
                        isSelected: selectedTab == 0,
                        action: { 
                            selectedTab = 0
                            // Optional haptic feedback
                            let impactFeedback = UIImpactFeedbackGenerator(style: .light)
                            impactFeedback.impactOccurred()
                        }
                    )
                    
                    Spacer()
                    
                    // Middle Tab (Settings/Menu)
                    BottomNavItem(
                        icon: "grid.circle",
                        isSelected: selectedTab == 1,
                        action: { 
                            selectedTab = 1
                            let impactFeedback = UIImpactFeedbackGenerator(style: .light)
                            impactFeedback.impactOccurred()
                        }
                    )
                    
                    Spacer()
                    
                    // Settings Tab
                    BottomNavItem(
                        icon: "gearshape.fill",
                        isSelected: selectedTab == 2,
                        action: { 
                            selectedTab = 2
                            let impactFeedback = UIImpactFeedbackGenerator(style: .light)
                            impactFeedback.impactOccurred()
                        }
                    )
                    
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
                .transition(.asymmetric(
                    insertion: .opacity.combined(with: .scale(scale: 0.9)),
                    removal: .opacity.combined(with: .scale(scale: 1.1))
                ))
            }
            
            // Active Tab Indicator (when collapsed) - Android style
            if !shouldShowFullNav && !shouldShowMessage {
                Button(action: {
                    withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
                        // Toggle the collapsed state
                        isManuallyCollapsed.toggle()
                    }
                }) {
                    Image(systemName: getActiveTabIcon())
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(AppConstants.Colors.primary)
                        .frame(width: 36, height: 36)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .transition(.asymmetric(
                    insertion: .scale(scale: 0.8).combined(with: .opacity),
                    removal: .scale(scale: 1.1).combined(with: .opacity)
                ))
            }
            
            // Collapse/Expand Indicator (hidden during message display)
            if !shouldShowMessage {
                Button(action: {
                    withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
                        isManuallyCollapsed.toggle()
                    }
                }) {
                    Rectangle()
                        .fill(Color.gray.opacity(0.3))
                        .frame(width: 40, height: 4)
                        .cornerRadius(2)
                        .rotationEffect(.degrees(shouldShowFullNav ? 0 : 180))
                }
                .padding(.bottom, 8)
            }
        }
        .frame(width: navBarWidth, height: navBarHeight)
        .background(
            RoundedRectangle(cornerRadius: shouldShowFullNav ? 20 : 25)
                .fill(Color.black)
                .shadow(color: Color.black.opacity(0.3), radius: 10, x: 0, y: -2)
        )
        .padding(.bottom, 8)
        .animation(.spring(response: 0.4, dampingFraction: 0.8), value: shouldShowFullNav)
        .animation(.spring(response: 0.4, dampingFraction: 0.8), value: scrollStateManager.isScrolling)
        .onTapGesture {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
                isManuallyCollapsed.toggle()
            }
        }
    }
    
    private func getActiveTabIcon() -> String {
        switch selectedTab {
        case 0: return "house.fill"
        case 1: return "grid.circle"
        case 2: return "gearshape.fill"
        default: return "house.fill"
        }
    }
}

// MARK: - Bottom Navigation Item
struct BottomNavItem: View {
    let icon: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(isSelected ? AppConstants.Colors.primary : .white)
                .frame(width: 44, height: 44)
        }
    }
}

// MARK: - Preview Components
#Preview("Balance Card") {
    BalanceCard(
        balance: "5.999964492 SOL",
        walletAddress: "EWbRTPxMAYqd45s7p5brfhJCwjWDFqq3g74uJ9SacMck",
        email: "dogukangundogan5@gmail.com",
        isRefreshing: false,
        onRefresh: {}
    )
}

#Preview("Action Buttons") {
    ActionButtons(
        onPayTapped: { print("Pay tapped") },
        onReceiveTapped: { print("Receive tapped") }
    )
}

#Preview("Portfolio Section") {
    let mockItems = [
        PortfolioItem(
            symbol: "SOL",
            name: "Solana",
            balance: Decimal(string: "5.999964492") ?? 0,
            iconColor: "primary"
        ),
        PortfolioItem(
            symbol: "USDC",
            name: "USD Coin",
            balance: Decimal(0),
            iconColor: "secondary"
        )
    ]
    
    PortfolioSection(portfolioItems: mockItems)
        .background(AppConstants.Colors.background)
}

#Preview("Account Information") {
    let mockProfile = UserProfile(
        id: "preview-id",
        email: "dogukangundogan5@gmail.com",
        displayName: "Dogukan Gundogan",
        solanaWalletAddress: "EWbRTPxMAYqd45s7p5brfhJCwjWDFqq3g74uJ9SacMck",
        ethWalletAddress: "",
        verifier: "web3auth"
    )
    
    AccountInformationSection(userProfile: mockProfile)
}

// MARK: - Message Display View
struct MessageDisplayView: View {
    let message: String
    
    var body: some View {
        VStack {
            Text(message)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
                .lineLimit(nil)
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
        }
        .frame(maxWidth: .infinity)
    }
}

#Preview("Custom Bottom Nav") {
    ZStack {
        Color.gray.opacity(0.1)
            .ignoresSafeArea()
        
        VStack {
            Spacer()
            HStack {
                Spacer()
                CustomBottomNavBar(
                    selectedTab: .constant(0),
                    scrollStateManager: ScrollStateManager()
                )
                Spacer()
            }
            .padding(.horizontal, 16)
        }
    }
}

#Preview("Message Display") {
    ZStack {
        Color.black
            .ignoresSafeArea()
        
        MessageDisplayView(message: "Payment successful! Transaction completed.")
            .background(Color.black)
            .cornerRadius(20)
            .padding()
    }
}

