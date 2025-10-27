//
//  SettingsViewState.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import Foundation

enum SettingsViewState {
    case showSettings
    case showPrivateKey(String)
    case showError(String)
    case externalSource(String)
}