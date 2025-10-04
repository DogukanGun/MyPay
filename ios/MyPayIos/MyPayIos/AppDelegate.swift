//
//  AppDelegate.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 16.09.25.
//

import FirebaseCore
import FirebaseAuth
import SwiftUI

class AppDelegate: NSObject, UIApplicationDelegate {
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()

    return true
  }
  
  func application(_ application: UIApplication, open url: URL,
                   options: [UIApplication.OpenURLOptionsKey: Any]) -> Bool {
    print("📱 App received URL: \(url.absoluteString)")
    print("📱 URL scheme: \(url.scheme ?? "no scheme")")
    print("📱 URL host: \(url.host ?? "no host")")
    
    // Handle Firebase Auth URLs
    if Auth.auth().canHandle(url) {
      print("✅ Firebase Auth handled the URL")
      return true
    }
    
    // Handle custom scheme URLs
    if url.scheme == "mypayios" {
      print("✅ Custom scheme handled")
      return true
    }
    
    // Handle Firebase project scheme
    if url.scheme == "nwallet-dbd53" {
      print("✅ Firebase project scheme detected")
      return Auth.auth().canHandle(url)
    }
    
    // Handle Google OAuth URLs
    if url.scheme?.hasPrefix("com.googleusercontent.apps") == true {
      print("✅ Google OAuth URL handled")
      return Auth.auth().canHandle(url)
    }
    
    print("❌ No handler found for URL")
    return false
  }
}
