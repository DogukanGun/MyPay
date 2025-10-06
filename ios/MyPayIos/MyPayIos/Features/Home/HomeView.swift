//
//  HomeView.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 11.09.25.
//

import SwiftUI

// MARK: - Home View
struct HomeView: View {
    @EnvironmentObject private var navigationCoordinator: NavigationCoordinator
    @StateObject private var viewModel = HomeViewModel()
    @Environment(\.scrollStateManager) private var scrollStateManager
    @EnvironmentObject private var messageManager: BottomNavMessageManager
    
    var body: some View {
        ZStack {
            AppConstants.Colors.background
                .ignoresSafeArea(.all)
            
            VStack(spacing: 0) {
                TopBar(
                    title: "Home",
                    showLogoutButton: true,
                    onLogoutTapped: {
                        viewModel.logout()
                    }
                )
                ScrollView {
                    VStack(spacing: 24) {
                        // Scroll Position Reader
                        ScrollPositionReader(
                            coordinateSpace: "homeScrollView"
                        ) { isScrolling, isScrollingUp in
                            scrollStateManager.updateScrollState(
                                isScrolling: isScrolling,
                                isScrollingUp: isScrollingUp
                            )
                        }
                        
                        // Chain Selector
                        ChainSelector(
                            selectedChain: viewModel.selectedChain,
                            onChainChanged: viewModel.switchChain
                        )
                        
                        // Balance Card
                        BalanceCard(
                            balance: viewModel.formattedBalance,
                            walletAddress: viewModel.formattedWalletAddress,
                            email: viewModel.displayName,
                            isRefreshing: viewModel.isRefreshing,
                            onRefresh: viewModel.refreshData
                        )
                        
                        // Action Buttons
                        ActionButtons(
                            onPayTapped: viewModel.handlePayAction,
                            onReceiveTapped: viewModel.handleReceiveAction
                        )
                        
                        // Portfolio Section
                        PortfolioSection(
                            portfolioItems: viewModel.portfolioItems,
                            selectedChain: viewModel.selectedChain
                        )
                        
                        // Account Information Section
                        AccountInformationSection(
                            userProfile: viewModel.userProfile
                        )
                        
                        // Bottom padding to account for bottom nav
                        Color.clear.frame(height: 120)
                    }
                    .padding(AppConstants.UI.padding)
                }
                .coordinateSpace(name: "homeScrollView")
                .scrollIndicators(.hidden)
                .refreshable {
                    viewModel.refreshData()
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            viewModel.loadUserData()
        }
        .sheet(isPresented: $viewModel.showPaymentSheet) {
            if viewModel.isSendMode {
                SendView(
                    userWalletAddress: viewModel.formattedWalletAddress,
                    amount: "",
                    selectedChain: viewModel.selectedChain,
                    onBackClick: viewModel.dismissPaymentSheet
                )
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
            } else {
                ReceiveView(
                    selectedChain: viewModel.selectedChain,
                    onChainChanged: viewModel.switchChain,
                    onBackClick: viewModel.dismissPaymentSheet,
                    onContinueClick: { amountValue, publicKey in
                        viewModel.initiateNFCPayment(amount: amountValue, publicKey: publicKey)
                    }
                )
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
            }
        }
    }
}

// MARK: - Preview
#Preview {
    HomeView()
} 
