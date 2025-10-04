//
//  AppRouter.swift
//  MyPayIos
//
//  Created by Claude Code on 15.09.25.
//

import SwiftUI
import FirebaseAuth

// MARK: - App Router View
struct AppRouter: View {
    @StateObject private var navigationCoordinator = NavigationCoordinator.shared
    @State private var showSplash = true
    
    var body: some View {
        ZStack {
            if showSplash {
                SplashView { destination in
                    handleSplashCompletion(destination)
                }
            } else {
                NavigationStack(path: $navigationCoordinator.navigationPath) {
                    if navigationCoordinator.currentDestination == .login {
                        LoginView()
                            .navigationDestination(for: Destination.self) { destination in
                                DestinationFactory.createView(for: destination)
                            }
                    } else {
                        MainContentView()
                            .navigationDestination(for: Destination.self) { destination in
                                DestinationFactory.createView(for: destination)
                            }
                    }
                }
                .environmentObject(navigationCoordinator)
                .environmentObject(BottomNavMessageManager.shared)
            }
        }
        .onOpenURL { url in
            handleDeepLink(url)
        }
    }
    
    // MARK: - Private Methods
    private func handleDeepLink(_ url: URL) {
        navigationCoordinator.handleDeepLink(url)
    }
    
    private func handleSplashCompletion(_ destination: Destination) {
        withAnimation(.easeInOut(duration: 0.5)) {
            showSplash = false
            navigationCoordinator.reset(to: destination)
        }
    }
}

// MARK: - Main Content View
private struct MainContentView: View {
    @EnvironmentObject private var navigationCoordinator: NavigationCoordinator
    @State private var selectedTab: Int = 0
    @StateObject private var scrollStateManager = ScrollStateManager()
    
    var body: some View {
        ZStack {
            // Main content based on selected tab
            Group {
                switch selectedTab {
                case 0:
                    HomeViewWrapper(scrollStateManager: scrollStateManager)
                case 1:
                    JarvisView()
                case 2:
                    SettingsView()
                default:
                    HomeViewWrapper(scrollStateManager: scrollStateManager)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            
            // Global Bottom Navigation
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    CustomBottomNavBar(
                        selectedTab: $selectedTab,
                        scrollStateManager: scrollStateManager
                    )
                    Spacer()
                }
                .padding(.horizontal, 16)
            }
        }
        .environment(\.scrollStateManager, scrollStateManager)
        .ignoresSafeArea(.keyboard, edges: .bottom)
    }
}

// MARK: - Home View Wrapper
private struct HomeViewWrapper: View {
    @ObservedObject var scrollStateManager: ScrollStateManager
    
    var body: some View {
        HomeView()
            .environment(\.scrollStateManager, scrollStateManager)
    }
}

// MARK: - Destination Factory
struct DestinationFactory {
    static func createView(for destination: Destination) -> some View {
        switch destination {
        case .splash:
            return AnyView(
                SplashView { _ in }
            )
        case .login:
            return AnyView(LoginView())
        case .home:
            return AnyView(HomeView())
        case .jarvis:
            return AnyView(JarvisView())
        case .settings:
            return AnyView(SettingsView())
        }
    }
}

// MARK: - Placeholder Views
private struct JarvisView: View {
    var body: some View {
        Text("Jarvis View")
            .navigationTitle("Jarvis")
    }
}
