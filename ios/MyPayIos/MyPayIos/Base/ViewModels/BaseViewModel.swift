//
//  BaseViewModel.swift
//  MyPayIos
//
//  Created by Claude Code on 15.09.25.
//

import Foundation
import Combine

// MARK: - Base View Model Protocol
protocol BaseViewModelProtocol: ObservableObject {
    var navigationCoordinator: NavigationCoordinator { get }
    var loadingState: LoadingState { get }
    var errorMessage: String? { get }
    
    func handleError(_ error: AppError)
    func clearError()
}

// MARK: - Base View Model Implementation
class BaseViewModel: BaseViewModelProtocol {
    
    // MARK: - Properties
    let navigationCoordinator: NavigationCoordinator =  NavigationCoordinator.shared
    
    @Published private(set) var loadingState: LoadingState = .idle
    @Published private(set) var errorMessage: String?
    
    var cancellables = Set<AnyCancellable>()
    
    // MARK: - Computed Properties
    var isLoading: Bool {
        loadingState.isLoading
    }
    
    var hasError: Bool {
        loadingState.hasError
    }
    
    // MARK: - Initialization
    init() {
        setupErrorHandling()
    }
    
    deinit {
        cancellables.removeAll()
    }
    
    // MARK: - Error Handling
    func handleError(_ error: AppError) {
        DispatchQueue.main.async { [weak self] in
            self?.loadingState = .error(error)
            self?.errorMessage = error.errorDescription
            print("❌ Error in \(String(describing: self)): \(error.errorDescription ?? "Unknown error")")
        }
    }
    
    func clearError() {
        DispatchQueue.main.async { [weak self] in
            self?.loadingState = .idle
            self?.errorMessage = nil
        }
    }
    
    // MARK: - Loading State Management
    func setLoading(_ isLoading: Bool) {
        DispatchQueue.main.async { [weak self] in
            self?.loadingState = isLoading ? .loading : .idle
            if !isLoading {
                self?.errorMessage = nil
            }
        }
    }
    
    func setSuccess() {
        DispatchQueue.main.async { [weak self] in
            self?.loadingState = .success
            self?.errorMessage = nil
        }
    }
    
    // MARK: - Private Methods
    private func setupErrorHandling() {
        $loadingState
            .sink { [weak self] state in
                switch state {
                case .error(let error):
                    self?.errorMessage = error.errorDescription
                case .success, .loading, .idle:
                    self?.errorMessage = nil
                }
            }
            .store(in: &cancellables)
    }
}

// MARK: - Navigation Helper Extension
extension BaseViewModel {
    func navigate(to destination: Destination) {
        navigationCoordinator.navigate(to: destination)
    }
    
    func navigateUp() {
        navigationCoordinator.navigateUp()
    }
    
    func reset(to destination: Destination) {
        navigationCoordinator.reset(to: destination)
    }
    
    func popToRoot() {
        navigationCoordinator.popToRoot()
    }
}

// MARK: - Async Helper Extension
extension BaseViewModel {
    /// Executes an async operation with proper loading state management
    func executeAsync<T>(
        operation: @escaping () async throws -> T,
        onSuccess: @escaping (T) -> Void = { _ in },
        onError: @escaping (AppError) -> Void = { _ in }
    ) {
        Task { @MainActor in
            setLoading(true)
            
            do {
                let result = try await operation()
                setSuccess()
                onSuccess(result)
            } catch {
                let appError = error as? AppError ?? .unknown(error.localizedDescription)
                handleError(appError)
                onError(appError)
            }
        }
    }
}
