//
//  SettingsComponents.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import SwiftUI

struct SettingCell: View {
    let settingData: SettingCellData
    
    var body: some View {
        Button(action: settingData.onTapped) {
            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(settingData.text)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.leading)
                    
                    if settingData.cellType == .info {
                        Text("Tap to copy and go back")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                
                Spacer()
                
                switch settingData.cellType {
                case .externalLink:
                    Image(systemName: "chevron.right")
                        .foregroundColor(.secondary)
                        .font(.system(size: 14, weight: .medium))
                        
                case .info:
                    Image(systemName: "doc.on.doc")
                        .foregroundColor(.blue)
                        .font(.system(size: 16, weight: .medium))
                        
                case .toggle:
                    Toggle("", isOn: .constant(false))
                        .labelsHidden()
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color(UIColor.systemBackground))
                    .shadow(color: .black.opacity(0.1), radius: 2, x: 0, y: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct SettingsHeader: View {
    let title: String
    
    var body: some View {
        HStack {
            Text(title)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.top, 16)
        .padding(.bottom, 8)
    }
}

struct SettingsSection: View {
    let title: String
    let settings: [SettingCellData]
    
    var body: some View {
        VStack(spacing: 12) {
            SettingsHeader(title: title)
            
            ForEach(settings.indices, id: \.self) { index in
                SettingCell(settingData: settings[index])
            }
        }
    }
}

#Preview {
    VStack(spacing: 16) {
        SettingCell(
            settingData: SettingCellData(
                text: "Private Key",
                cellType: .externalLink,
                onTapped: {}
            )
        )
        
        SettingCell(
            settingData: SettingCellData(
                text: "0x1234567890abcdef...",
                cellType: .info,
                onTapped: {}
            )
        )
        
        SettingCell(
            settingData: SettingCellData(
                text: "Enable Notifications",
                cellType: .toggle,
                onTapped: {}
            )
        )
    }
    .padding()
}