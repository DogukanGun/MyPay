//
//  SettingsView.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 25.09.25.
//

import SwiftUI
import MessageUI

struct SettingsView: View {
    @StateObject private var viewModel = SettingsViewModel()
    @EnvironmentObject private var navigationCoordinator: NavigationCoordinator
    
    var body: some View {
        ZStack {
            AppConstants.Colors.background
                .ignoresSafeArea(.all)
            
            VStack(spacing: 0) {
                TopBar(
                    title: "Settings",
                    showLogoutButton: true,
                    onLogoutTapped: {
                        viewModel.logout()
                    }
                )
                ScrollView {
                    VStack(spacing: 16) {
                        switch viewModel.viewState {
                        case .showSettings:
                            ForEach(Settings.allCases, id: \.self) { setting in
                                SettingCell(
                                    settingData: SettingCellData(
                                        text: setting.displayName,
                                        cellType: setting.cellType,
                                        onTapped: {
                                            viewModel.executeSetting(setting)
                                        }
                                    )
                                )
                            }
                            
                        case .showPrivateKey(let privateKey):
                            SettingCell(
                                settingData: SettingCellData(
                                    text: privateKey,
                                    cellType: .info,
                                    onTapped: {
                                        UIPasteboard.general.string = privateKey
                                        viewModel.showList()
                                    }
                                )
                            )
                            
                        case .showError(let errorMessage):
                            Text(errorMessage)
                                .foregroundColor(.red)
                                .padding()
                            
                        case .externalSource:
                            EmptyView()
                        }
                    }
                    .padding(16)
                }
            }
        }
        .navigationBarHidden(true)
        .alert(viewModel.alertTitle, isPresented: $viewModel.showAlert) {
            if viewModel.alertTitle == "Feedback Form" {
                Button("Send") {
                    viewModel.sendFeedbackEmail()
                }
                Button("Cancel", role: .cancel) { }
            } else {
                Button("OK") { }
            }
        } message: {
            Text(viewModel.alertMessage)
        }
        .sheet(isPresented: $viewModel.showMailComposer) {
            MailComposeView(
                recipients: ["dogukangundogan5@gmail.com"],
                subject: "App Feedback"
            )
        }
    }
}

struct MailComposeView: UIViewControllerRepresentable {
    let recipients: [String]
    let subject: String
    
    func makeUIViewController(context: Context) -> MFMailComposeViewController {
        let composer = MFMailComposeViewController()
        composer.mailComposeDelegate = context.coordinator
        composer.setToRecipients(recipients)
        composer.setSubject(subject)
        return composer
    }
    
    func updateUIViewController(_ uiViewController: MFMailComposeViewController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }
    
    class Coordinator: NSObject, MFMailComposeViewControllerDelegate {
        func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
            controller.dismiss(animated: true)
        }
    }
}

#Preview {
    SettingsView()
}