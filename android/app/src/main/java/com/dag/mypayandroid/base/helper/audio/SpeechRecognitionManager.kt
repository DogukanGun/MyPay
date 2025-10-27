package com.dag.mypayandroid.base.helper.audio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    private val _recognitionState = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val recognitionState: StateFlow<SpeechRecognitionState> = _recognitionState.asStateFlow()
    
    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()
    
    companion object {
        private const val TAG = "SpeechRecognitionManager"
    }
    
    sealed class SpeechRecognitionState {
        object Idle : SpeechRecognitionState()
        object Listening : SpeechRecognitionState()
        data class Ready(val text: String) : SpeechRecognitionState()
        data class Error(val message: String) : SpeechRecognitionState()
    }
    
    fun startListening(): Boolean {
        if (!hasAudioPermission()) {
            _recognitionState.value = SpeechRecognitionState.Error("Audio permission not granted")
            return false
        }
        
        if (isListening) {
            Log.w(TAG, "Already listening")
            return false
        }
        
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _recognitionState.value = SpeechRecognitionState.Error("Speech recognition not available")
            return false
        }
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    isListening = true
                    _recognitionState.value = SpeechRecognitionState.Listening
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Speech started")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Can be used for audio level visualization
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    Log.d(TAG, "Speech ended")
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error: $error"
                    }
                    Log.e(TAG, "Recognition error: $errorMessage")
                    isListening = false
                    _recognitionState.value = SpeechRecognitionState.Error(errorMessage)
                }
                
                override fun onResults(results: Bundle?) {
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                        if (matches.isNotEmpty()) {
                            val text = matches[0]
                            Log.d(TAG, "Final result: $text")
                            _transcribedText.value = text
                            _recognitionState.value = SpeechRecognitionState.Ready(text)
                        }
                    }
                    isListening = false
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                        if (matches.isNotEmpty()) {
                            val text = matches[0]
                            Log.d(TAG, "Partial result: $text")
                            _transcribedText.value = text
                        }
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            speechRecognizer?.startListening(recognitionIntent)
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            _recognitionState.value = SpeechRecognitionState.Error("Failed to start: ${e.message}")
            cleanup()
            return false
        }
    }
    
    fun stopListening() {
        if (!isListening) {
            Log.w(TAG, "Not currently listening")
            return
        }
        
        try {
            speechRecognizer?.stopListening()
            isListening = false
            Log.d(TAG, "Stopped listening")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
            _recognitionState.value = SpeechRecognitionState.Error("Failed to stop: ${e.message}")
            cleanup()
        }
    }
    
    fun getTranscribedText(): String {
        return _transcribedText.value
    }
    
    fun clearTranscription() {
        _transcribedText.value = ""
        _recognitionState.value = SpeechRecognitionState.Idle
        cleanup()
    }
    
    private fun cleanup() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun isCurrentlyListening(): Boolean = isListening
}

