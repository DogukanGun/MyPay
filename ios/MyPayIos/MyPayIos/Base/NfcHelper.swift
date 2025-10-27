//
//  NfcHelper.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 08.10.25.
//

import CoreNFC
import CryptoKit

class NfcHelper: NSObject, ObservableObject, NFCNDEFReaderSessionDelegate, NFCTagReaderSessionDelegate  {
    
    static let shared = NfcHelper()
    private var type = "application/vnd.mypayios.solanapay"
    public var endAlert: String?
    var message: String?
    public var startAlert: String? = "Hold your iPhone near the tag."
    var onMessageReceived: ((_ msg: String) -> Void)?
    var onPaymentUrlReceived: ((_ url: String) -> Void)?
    
    private let solanaHelper = SolanaHelper.shared
    
    private override init() {
        super.init()
    }
    
    public var ndefSession: NFCNDEFReaderSession?
    public var tagSession: NFCTagReaderSession?
    
    // HCE Communication Constants (match Android)
    private let AID = "F0010203040506"
    private let SELECT_APDU_HEADER = "00A40400"
    
    // MARK: - NFCNDEFReaderSessionDelegate
    func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: any Error) {
        print("NDEF Session invalidated: \(error)")
    }
    
    // MARK: - NFCTagReaderSessionDelegate  
    func tagReaderSession(_ session: NFCTagReaderSession, didInvalidateWithError error: Error) {
        print("Tag Session invalidated: \(error)")
    }
    
    func tagReaderSessionDidBecomeActive(_ session: NFCTagReaderSession) {
        // Required delegate method - session is now active and ready to detect tags
        print("Tag reader session became active")
    }
    
    func tagReaderSession(_ session: NFCTagReaderSession, didDetect tags: [NFCTag]) {
        guard let tag = tags.first else { return }
        
        session.connect(to: tag) { [weak self] error in
            if let error = error {
                session.invalidate(errorMessage: "Connection failed: \(error.localizedDescription)")
                return
            }
            
            self?.communicateWithHCE(tag: tag, session: session)
        }
    }
    
    private func communicateWithHCE(tag: NFCTag, session: NFCTagReaderSession) {
        guard case let .iso7816(iso7816Tag) = tag else {
            session.invalidate(errorMessage: "Tag doesn't support ISO7816")
            return
        }
        
        // Build SELECT command to match Android HCE service
        let aidData = hexStringToData(AID)
        let selectCommand = NFCISO7816APDU(
            instructionClass: 0x00,
            instructionCode: 0xA4,
            p1Parameter: 0x04,
            p2Parameter: 0x00,
            data: aidData,
            expectedResponseLength: -1
        )
        
        iso7816Tag.sendCommand(apdu: selectCommand) { [weak self] response, sw1, sw2, error in
            if let error = error {
                session.invalidate(errorMessage: "Communication failed: \(error.localizedDescription)")
                return
            }
            
            // Check for success response (0x9000)
            if sw1 == 0x90 && sw2 == 0x00 {
                let paymentData = String(data: response, encoding: .utf8) ?? ""
                print("Received payment data from HCE: \(paymentData)")
                
                DispatchQueue.main.async {
                    if paymentData.hasPrefix(SolanaPayConstants.solanaProtocol) {
                        self?.onPaymentUrlReceived?(paymentData)
                        session.alertMessage = self?.endAlert ?? "Payment URL received successfully!"
                    } else {
                        self?.onMessageReceived?(paymentData)
                        session.alertMessage = self?.endAlert ?? "Payment data received successfully!"
                    }
                    session.invalidate()
                }
            } else {
                session.invalidate(errorMessage: "HCE service returned error: \(sw1)\(sw2)")
            }
        }
    }
    
    private func hexStringToData(_ hex: String) -> Data {
        var data = Data()
        var temp = hex
        while !temp.isEmpty {
            let substring = String(temp.prefix(2))
            temp = String(temp.dropFirst(2))
            if let byte = UInt8(substring, radix: 16) {
                data.append(byte)
            }
        }
        return data
    }
    
    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        DispatchQueue.main.async {
            for message in messages {
                for record in message.records {
                    // Check if this is our custom media type
                    let typeString = String(decoding: record.type, as: UTF8.self)
                    if typeString == self.type {
                        let receivedMessage = String(decoding: record.payload, as: UTF8.self)
                        self.message = receivedMessage
                        
                        // Check if the message is a Solana Pay URL
                        if receivedMessage.hasPrefix(SolanaPayConstants.solanaProtocol) {
                            self.onPaymentUrlReceived?(receivedMessage)
                            session.alertMessage = self.endAlert ?? "Solana Pay URL received successfully!"
                        } else {
                            self.onMessageReceived?(receivedMessage)
                            session.alertMessage = self.endAlert ?? "Payment data received successfully!"
                        }
                        return
                    }
                }
            }
            
            // Fallback for other NDEF messages
            let receivedMessage = messages.map {
                $0.records.map {
                    String(decoding: $0.payload, as: UTF8.self)
                }.joined(separator: "\n")
            }.joined(separator: " ")
            
            self.message = receivedMessage
            self.onMessageReceived?(receivedMessage)
            session.alertMessage = self.endAlert ?? "Read \(messages.count) NDEF Messages, and \(messages[0].records.count) Records."
        }
    }
    
    
    func readerSession(_ session: NFCNDEFReaderSession, didDetect tags: [any NFCNDEFTag]) {
        if tags.count > 1 {
            let retryInterval = DispatchTimeInterval.milliseconds(500)
            session.alertMessage = "Detected more than 1 phone. Please try again."
            DispatchQueue.global().asyncAfter(deadline: .now() + retryInterval, execute: {
                session.restartPolling()
            })
            return
        }
        
        let tag = tags.first!
        session.connect(to: tag, completionHandler: { (error: Error?) in
            if nil != error {
                session.alertMessage = "Unable to connect to phone."
                session.invalidate()
                return
            }
            
            tag.queryNDEFStatus(completionHandler: { (ndefStatus: NFCNDEFStatus, capacity: Int, error: Error?) in
                guard error == nil else {
                    session.alertMessage = "Unable to query the status of phone."
                    session.invalidate()
                    return
                }
                switch ndefStatus {
                case .notSupported:
                    session.alertMessage = "Tag is not NDEF compliant."
                    session.invalidate()
                case .readOnly:
                    session.alertMessage = "Read only phone detected."
                    session.invalidate()
                case .readWrite:
                    let payload: NFCNDEFPayload?
                    payload = NFCNDEFPayload.init(
                        format: .media,
                        type: Data(self.type.utf8),
                        identifier: Data(),
                        payload: Data("\(String(describing: self.message))".utf8)
                    )
                    let message = NFCNDEFMessage(records: [payload].compactMap({ $0 }))
                    tag.writeNDEF(message, completionHandler: { (error: Error?) in
                        if nil != error {
                            session.alertMessage = "Write to tag fail: \(error!)"
                        } else {
                            session.alertMessage = self.endAlert ?? "Write \(String(describing: self.message)) to tag successful."
                        }
                        session.invalidate()
                    })
                @unknown default:
                    session.alertMessage = "Unknown tag status."
                    session.invalidate()
                }
            })
        })
    }
    
    func nfcWrite() {
        ndefSession = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: true)
        if let startAlert = self.startAlert {
            ndefSession?.alertMessage = startAlert
        }
        ndefSession?.begin()
    }
    
    func writePaymentUrlToNFC(url: String) {
        self.message = url
        self.endAlert = "Payment URL written to NFC successfully!"
        self.startAlert = "Hold your iPhone near another device to share payment info"
        nfcWrite()
    }
    
    // MARK: - NFC Reading for Solana Pay
    
    func startNFCReading(onPaymentReceived: @escaping (String) -> Void) {
        self.onPaymentUrlReceived = onPaymentReceived
        
        // Stop any existing sessions first
        stopNFCReading()
        
        // Use tag session to communicate with HCE service directly
        tagSession = NFCTagReaderSession(pollingOption: [.iso14443], delegate: self, queue: nil)
        tagSession?.alertMessage = "Ready to receive payment - tap devices together"
        tagSession?.begin()
    }
    
    func stopNFCReading() {
        ndefSession?.invalidate()
        tagSession?.invalidate()
        ndefSession = nil
        tagSession = nil
        onPaymentUrlReceived = nil
        onMessageReceived = nil
    }
    
    // MARK: - Solana Pay Integration
    
    func processSolanaPayment(
        paymentUrl: String,
        completion: @escaping (Result<SolanaTransaction, Error>) -> Void
    ) {
        // Get private key from wallet manager
        WalletManager.shared.getPrivateKey(for: .solana) { [weak self] privateKey in
            guard let privateKey = privateKey else {
                completion(.failure(AppError.wallet(.walletNotFound)))
                return
            }
            
            // Process the payment using SolanaHelper
            self?.solanaHelper.receiveSolanaPayAndMakePayment(
                paymentUrl: paymentUrl,
                privateKey: privateKey,
                completion: completion
            )
        }
    }
}
