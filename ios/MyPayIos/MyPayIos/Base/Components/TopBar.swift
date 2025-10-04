//
//  TopBar.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import SwiftUI

struct TopBar: View {
    let title: String
    let showLogoutButton: Bool
    let onLogoutTapped: (() -> Void)?
    
    init(title: String, showLogoutButton: Bool = false, onLogoutTapped: (() -> Void)? = nil) {
        self.title = title
        self.showLogoutButton = showLogoutButton
        self.onLogoutTapped = onLogoutTapped
    }
    
    var body: some View {
        HStack {
            Text(title)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.white)
            
            Spacer()
            
            if showLogoutButton {
                Button(action: {
                    onLogoutTapped?()
                }) {
                    Image(systemName: "rectangle.portrait.and.arrow.right")
                        .font(.system(size: 20, weight: .medium))
                        .foregroundColor(.white)
                }
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(
            Color.black
                .ignoresSafeArea(edges: .top)
        )
    }
}

#Preview {
    VStack {
        TopBar(
            title: "Home",
            showLogoutButton: true,
            onLogoutTapped: {
                print("Logout tapped")
            }
        )
        
        Spacer()
    }
    .background(Color.gray.opacity(0.1))
}