//
//  NavigationAction.swift
//  MyPayIos
//
//  Created by Claude Code on 15.09.25.
//

import Foundation

// MARK: - Navigation Actions
enum NavigationAction {
    case navigate(to: Destination)
    case navigateUp
    case reset(to: Destination)
    case popToRoot
}