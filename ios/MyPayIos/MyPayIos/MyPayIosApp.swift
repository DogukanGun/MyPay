//
//  MyPayIosApp.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 09.08.25.
//

import SwiftUI
import SwiftData

@main
struct MyPayIosApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            AppRouter()
        }
    }
}
