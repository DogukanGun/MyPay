//
//  JarvisViewModel.swift
//  MyPayIos
//
//  Created by AI Assistant on 16.10.25.
//

import Foundation
import Speech
import AVFoundation
import Combine

// MARK: - Jarvis View Model
final class JarvisViewModel: BaseViewModel {
    
    // MARK: - Published Properties
    @Published private(set) var viewState: JarvisViewState = .idle
    @Published private(set) var isListening: Bool = false
    @Published private(set) var currentCard: CardData?
    
    // MARK: - Private Properties
    private var audioEngine: AVAudioEngine?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "en-US"))
    private var transcribedText: String = ""
    private var cardDismissTask: Task<Void, Never>?
    
    // MARK: - Initialization
    override init() {
        super.init()
        checkPermission()
    }
    
    deinit {
        stopListening()
        cardDismissTask?.cancel()
    }
    
    // MARK: - Public Methods
    func checkPermission() {
        let authStatus = SFSpeechRecognizer.authorizationStatus()
        
        switch authStatus {
        case .notDetermined:
            viewState = .askPermission
            requestPermission()
        case .denied, .restricted:
            viewState = .error(message: "Microphone access denied")
        case .authorized:
            viewState = .idle
        @unknown default:
            viewState = .error(message: "Unknown permission status")
        }
    }
    
    func requestPermission() {
        SFSpeechRecognizer.requestAuthorization { [weak self] authStatus in
            DispatchQueue.main.async {
                switch authStatus {
                case .authorized:
                    self?.viewState = .idle
                case .denied, .restricted:
                    self?.viewState = .error(message: "Microphone access denied")
                default:
                    self?.viewState = .error(message: "Permission request failed")
                }
            }
        }
    }
    
    func handleAudioPermissionResult(_ isGranted: Bool) {
        if isGranted {
            viewState = .idle
        } else {
            viewState = .error(message: "Microphone permission denied")
            showCard(message: "Please enable microphone access in Settings", type: .error)
        }
    }
    
    func startListening() {
        guard viewState == .idle else { return }
        
        // Check microphone permission
        AVAudioSession.sharedInstance().requestRecordPermission { [weak self] granted in
            DispatchQueue.main.async {
                if granted {
                    self?.beginRecognition()
                } else {
                    self?.viewState = .error(message: "Microphone permission denied")
                    self?.showCard(message: "Please enable microphone access in Settings", type: .error)
                }
            }
        }
    }
    
    func stopListening() {
        isListening = false
        
        // Stop audio engine
        audioEngine?.stop()
        audioEngine?.inputNode.removeTap(onBus: 0)
        
        // End recognition
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()
        
        recognitionRequest = nil
        recognitionTask = nil
        audioEngine = nil
        
        // Show confirmation dialog with transcribed text
        if !transcribedText.isEmpty {
            viewState = .confirming(transcription: transcribedText)
        } else {
            viewState = .idle
            showCard(message: "No voice input detected", type: .error)
        }
    }
    
    func confirmAndSend() {
        guard case .confirming(let transcription) = viewState else { return }
        
        viewState = .processing
        showCard(message: "Sending to Jarvis...", type: .processing)
        sendToBackend(transcription: transcription)
    }
    
    func cancelConfirmation() {
        transcribedText = ""
        viewState = .idle
        hideCard()
    }
    
    func resetToIdle() {
        transcribedText = ""
        viewState = .idle
        hideCard()
    }
    
    func sendToBackend(transcription: String? = nil) {
        let textToSend = transcription ?? transcribedText
        
        guard !textToSend.isEmpty else {
            viewState = .error(message: "No transcription available")
            showCard(message: "No voice input detected", type: .error)
            return
        }
        
        // Call Jarvis API
        JarvisApi.shared.ask(input: textToSend) { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    self?.viewState = .success(message: response.output)
                    self?.transcribedText = ""
                    self?.showCard(message: response.output, type: .success, duration: 5.0)
                    
                case .failure(let error):
                    print("ERROR: Jarvis API failed")
                    print("ERROR: \(error.errorDescription ?? "Unknown error")")
                    print("ERROR: Code: \(error.code)")
                    
                    let errorMsg = error.errorDescription ?? "Failed to process request"
                    self?.viewState = .error(message: errorMsg)
                    self?.transcribedText = ""
                    self?.showCard(message: "Error: \(errorMsg)", type: .error, duration: 5.0)
                }
            }
        }
    }
    
    func hideCard() {
        cardDismissTask?.cancel()
        currentCard = nil
    }
    
    // MARK: - Private Methods
    private func beginRecognition() {
        // Cancel any ongoing recognition
        recognitionTask?.cancel()
        recognitionTask = nil
        
        // Configure audio session
        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
            try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
            viewState = .error(message: "Audio session setup failed")
            return
        }
        
        // Create recognition request
        recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
        
        guard let recognitionRequest = recognitionRequest else {
            viewState = .error(message: "Unable to create recognition request")
            return
        }
        
        recognitionRequest.shouldReportPartialResults = true
        
        // Configure audio engine
        audioEngine = AVAudioEngine()
        guard let audioEngine = audioEngine else {
            viewState = .error(message: "Unable to create audio engine")
            return
        }
        
        let inputNode = audioEngine.inputNode
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { [weak self] buffer, _ in
            self?.recognitionRequest?.append(buffer)
        }
        
        // Start audio engine
        audioEngine.prepare()
        do {
            try audioEngine.start()
        } catch {
            viewState = .error(message: "Audio engine couldn't start")
            return
        }
        
        // Start recognition
        transcribedText = ""
        isListening = true
        viewState = .listening
        
        recognitionTask = speechRecognizer?.recognitionTask(with: recognitionRequest) { [weak self] result, error in
            guard let self = self else { return }
            
            if let result = result {
                let transcription = result.bestTranscription.formattedString
                DispatchQueue.main.async {
                    // Only update if transcription is not empty
                    if !transcription.isEmpty {
                        self.transcribedText = transcription
                        print("Transcribed: \(self.transcribedText)")
                    }
                }
            }
            
            if error != nil || result?.isFinal == true {
                // Don't call stopListening if user manually stopped
                // stopListening is already called by user action
                if error != nil {
                    DispatchQueue.main.async {
                        self.stopListening()
                    }
                }
            }
        }
    }
    
    private func showCard(message: String, type: CardData.CardType, duration: TimeInterval = 3.0) {
        cardDismissTask?.cancel()
        
        currentCard = CardData(message: message, type: type)
        
        cardDismissTask = Task { [weak self] in
            try? await Task.sleep(nanoseconds: UInt64(duration * 1_000_000_000))
            
            guard !Task.isCancelled else { return }
            
            await MainActor.run {
                self?.currentCard = nil
            }
        }
    }
    
}

