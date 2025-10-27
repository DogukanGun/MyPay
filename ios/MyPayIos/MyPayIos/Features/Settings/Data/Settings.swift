//
//  Settings.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import Foundation

enum Settings: String, CaseIterable {
    case privateKey = "PRIVATE_KEY"
    case feedbackForm = "FEEDBACK_FORM"
    case legal = "LEGAL"
    
    var cellType: SettingCellType {
        switch self {
        case .privateKey:
            return .externalLink
        case .feedbackForm:
            return .externalLink
        case .legal:
            return .externalLink
        }
    }
    
    var displayName: String {
        switch self {
        case .privateKey:
            return "Private Key"
        case .feedbackForm:
            return "Feedback Form"
        case .legal:
            return "Legal"
        }
    }
}

enum SettingCellType {
    case toggle
    case externalLink
    case info
}

struct SettingCellData {
    let text: String
    let cellType: SettingCellType
    let onTapped: () -> Void
    let onValueChange: ((Any) -> Void)?
    
    init(text: String, cellType: SettingCellType, onTapped: @escaping () -> Void, onValueChange: ((Any) -> Void)? = nil) {
        self.text = text
        self.cellType = cellType
        self.onTapped = onTapped
        self.onValueChange = onValueChange
    }
}