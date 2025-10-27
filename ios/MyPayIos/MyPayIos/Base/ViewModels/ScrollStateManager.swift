//
//  ScrollStateManager.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 18.09.25.
//

import Foundation
import Combine
import SwiftUI

// MARK: - Scroll State Data Model
struct ScrollState {
    let isScrolling: Bool
    let isScrollingUp: Bool
    
    init(isScrolling: Bool = false, isScrollingUp: Bool = true) {
        self.isScrolling = isScrolling
        self.isScrollingUp = isScrollingUp
    }
}

// MARK: - Scroll State Manager
class ScrollStateManager: ObservableObject {
    @Published private var _scrollState = ScrollState()
    
    var scrollState: ScrollState {
        _scrollState
    }
    
    var isScrolling: Bool {
        _scrollState.isScrolling
    }
    
    var isScrollingUp: Bool {
        _scrollState.isScrollingUp
    }
    
    // MARK: - Public Methods
    func updateScrolling(_ isScrolling: Bool) {
        _scrollState = ScrollState(
            isScrolling: isScrolling,
            isScrollingUp: _scrollState.isScrollingUp
        )
    }
    
    func updateScrollDirection(_ isScrollingUp: Bool) {
        _scrollState = ScrollState(
            isScrolling: _scrollState.isScrolling,
            isScrollingUp: isScrollingUp
        )
    }
    
    func updateScrollState(isScrolling: Bool, isScrollingUp: Bool) {
        _scrollState = ScrollState(
            isScrolling: isScrolling,
            isScrollingUp: isScrollingUp
        )
    }
    
    func toggle() {
        _scrollState = ScrollState(
            isScrolling: !_scrollState.isScrolling,
            isScrollingUp: _scrollState.isScrollingUp
        )
    }
    
    // MARK: - Convenience Methods
    func shouldShowBottomNav() -> Bool {
        // Show bottom nav when not scrolling or scrolling up
        return !isScrolling || isScrollingUp
    }
    
    func shouldCollapseBottomNav() -> Bool {
        // Collapse when scrolling down
        return isScrolling && !isScrollingUp
    }
}

// MARK: - Environment Key for ScrollStateManager
struct ScrollStateManagerKey: EnvironmentKey {
    static let defaultValue = ScrollStateManager()
}

extension EnvironmentValues {
    var scrollStateManager: ScrollStateManager {
        get { self[ScrollStateManagerKey.self] }
        set { self[ScrollStateManagerKey.self] = newValue }
    }
}

// MARK: - SwiftUI View Extension for Scroll Detection
extension View {
    func onScrollChanged(_ action: @escaping (Bool, Bool) -> Void) -> some View {
        self.background(
            ScrollDetectionView(onScrollChanged: action)
        )
    }
}

// MARK: - Scroll Detection View
struct ScrollDetectionView: UIViewRepresentable {
    let onScrollChanged: (Bool, Bool) -> Void
    
    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {
        // This will be handled by the ScrollView's coordinate space
    }
}

// MARK: - Scroll Position Reader
struct ScrollPositionReader: View {
    let coordinateSpace: String
    let onScrollChanged: (Bool, Bool) -> Void
    
    @State private var lastOffset: CGFloat = 0
    @State private var isScrolling: Bool = false
    @State private var scrollTimer: Timer?
    
    private let scrollThreshold: CGFloat = 1.0
    
    var body: some View {
        GeometryReader { geometry in
            Color.clear
                .preference(
                    key: ScrollOffsetPreferenceKey.self,
                    value: geometry.frame(in: .named(coordinateSpace)).minY
                )
        }
        .onPreferenceChange(ScrollOffsetPreferenceKey.self, perform: handleScrollOffset)
    }
    
    private func handleScrollOffset(_ offset: CGFloat) {
        let offsetDelta = offset - lastOffset
        let isScrollingUp = offsetDelta > 0
        
        // Only update if scroll delta is significant enough
        guard abs(offsetDelta) > scrollThreshold else { return }
        
        // Start scrolling if not already scrolling
        if !isScrolling {
            isScrolling = true
            onScrollChanged(true, isScrollingUp)
        } else {
            // Update direction if it changed
            onScrollChanged(true, isScrollingUp)
        }
        
        lastOffset = offset
        
        // Reset scrolling state after delay
        resetScrollTimer(isScrollingUp: isScrollingUp)
    }
    
    private func resetScrollTimer(isScrollingUp: Bool) {
        scrollTimer?.invalidate()
        scrollTimer = Timer.scheduledTimer(
            withTimeInterval: AppConstants.Animation.scrollDetectionDelay,
            repeats: false
        ) { _ in
            isScrolling = false
            onScrollChanged(false, isScrollingUp)
        }
    }
}

// MARK: - Scroll Offset Preference Key
struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
} 