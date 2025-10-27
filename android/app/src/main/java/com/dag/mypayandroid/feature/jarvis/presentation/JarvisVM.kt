package com.dag.mypayandroid.feature.jarvis.presentation

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.data.AlertDialogButton
import com.dag.mypayandroid.base.data.AlertDialogButtonType
import com.dag.mypayandroid.base.data.AlertDialogModel
import com.dag.mypayandroid.base.data.repository.JarvisRepository
import com.dag.mypayandroid.base.helper.audio.SpeechRecognitionManager
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JarvisVM @Inject constructor(
    val processHandler: ProcessHandler,
    private val speechRecognitionManager: SpeechRecognitionManager,
    private val jarvisRepository: JarvisRepository,
    private val alertDialogManager: AlertDialogManager
): BaseVM<JarvisVS>(JarvisVS.Idle) {

    companion object {
        private const val TAG = "JarvisVM"
    }
    
    val isListening: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            _viewState.collect { state ->
                value = state is JarvisVS.Listening
            }
        }
    }.asStateFlow()
    
    private var transcribedText = ""
    
    init {
        // Check permissions on startup like iOS
        checkPermission()
        
        // Observe speech recognition state changes
        viewModelScope.launch {
            speechRecognitionManager.recognitionState.collect { recognitionState ->
                when (recognitionState) {
                    is SpeechRecognitionManager.SpeechRecognitionState.Idle -> {
                        // Don't change state if we're confirming or processing
                        if (_viewState.value !is JarvisVS.Confirming && 
                            _viewState.value !is JarvisVS.Processing) {
                            _viewState.value = JarvisVS.Idle
                        }
                    }
                    is SpeechRecognitionManager.SpeechRecognitionState.Listening -> {
                        _viewState.value = JarvisVS.Listening
                    }
                    is SpeechRecognitionManager.SpeechRecognitionState.Ready -> {
                        transcribedText = recognitionState.text
                        showConfirmationDialog(recognitionState.text)
                    }
                    is SpeechRecognitionManager.SpeechRecognitionState.Error -> {
                        _viewState.value = JarvisVS.Error(recognitionState.message)
                        processHandler.show("Error: ${recognitionState.message}", duration = 3000L)
                    }
                }
            }
        }
        
        // Observe partial transcription results
        viewModelScope.launch {
            speechRecognitionManager.transcribedText.collect { text ->
                if (text.isNotEmpty() && _viewState.value is JarvisVS.Listening) {
                    Log.d(TAG, "Transcribed: $text")
                }
            }
        }
    }
    
    fun startListening() {
        // Check permission first like iOS does
        if (!speechRecognitionManager.hasAudioPermission()) {
            _viewState.value = JarvisVS.AskPermission
            return
        }
        
        if (speechRecognitionManager.startListening()) {
            _viewState.value = JarvisVS.Listening
        }
    }
    
    fun stopListening() {
        speechRecognitionManager.stopListening()
    }
    
    private fun showConfirmationDialog(transcription: String) {
        if (transcription.isEmpty()) {
            _viewState.value = JarvisVS.Idle
            processHandler.show("No voice input detected", duration = 3000L)
            return
        }
        
        _viewState.value = JarvisVS.Confirming(transcription)
        
        viewModelScope.launch {
            alertDialogManager.showAlert(
                AlertDialogModel(
                    title = "Confirm Your Message",
                    message = "You said: \"$transcription\"\n\nDo you approve this to be sent?",
                    positiveButton = AlertDialogButton(
                        text = "Send",
                        onClick = { confirmAndSend() },
                        type = AlertDialogButtonType.CUSTOM
                    ),
                    negativeButton = AlertDialogButton(
                        text = "Cancel",
                        onClick = { cancelConfirmation() },
                        type = AlertDialogButtonType.CLOSE
                    )
                )
            )
        }
    }
    
    fun confirmAndSend() {
        if (transcribedText.isEmpty()) {
            _viewState.value = JarvisVS.Idle
            processHandler.show("No voice input detected", duration = 3000L)
            return
        }
        
        _viewState.value = JarvisVS.Processing
        processHandler.show("Sending to Jarvis...")
        
        sendToBackend(transcribedText)
    }
    
    fun cancelConfirmation() {
        transcribedText = ""
        speechRecognitionManager.clearTranscription()
        _viewState.value = JarvisVS.Idle
        processHandler.hide()
    }
    
    fun showStatusCard(
        message: String,
        duration: Long? = null,
        condition: (() -> Boolean)? = null,
        backgroundColor: Color? = null
    ) {
        processHandler.show(message, duration, condition, backgroundColor)
    }
    
    fun hideStatusCard() {
        processHandler.hide()
    }
    
    fun sendToBackend(input: String? = null) {
        val textToSend = input ?: transcribedText
        
        if (textToSend.isEmpty()) {
            _viewState.value = JarvisVS.Error("No transcription available")
            processHandler.show("No voice input detected", duration = 3000L)
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending to Jarvis API: $textToSend")
                
                val result = jarvisRepository.ask(textToSend)
                
                result.onSuccess { response ->
                    Log.d(TAG, "Jarvis success - output: ${response.output}")
                    _viewState.value = JarvisVS.Success(response.output)
                    transcribedText = ""
                    speechRecognitionManager.clearTranscription()
                    processHandler.show(response.output, duration = 5000L)
                }.onFailure { error ->
                    Log.e(TAG, "Jarvis API failed", error)
                    val errorMsg = error.message ?: "Failed to process request"
                    _viewState.value = JarvisVS.Error(errorMsg)
                    transcribedText = ""
                    speechRecognitionManager.clearTranscription()
                    processHandler.show("Error: $errorMsg", duration = 5000L)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during sendToBackend", e)
                _viewState.value = JarvisVS.Error(e.message ?: "Unknown error")
                transcribedText = ""
                speechRecognitionManager.clearTranscription()
                processHandler.show("Error: ${e.message}", duration = 5000L)
            }
        }
    }
    
    fun resetToIdle() {
        transcribedText = ""
        speechRecognitionManager.clearTranscription()
        _viewState.value = JarvisVS.Idle
        processHandler.hide()
    }

    fun checkPermission() {
        val isPermissionGranted = speechRecognitionManager.hasAudioPermission()
        if (!isPermissionGranted) {
            _viewState.value = JarvisVS.AskPermission
        } else {
            _viewState.value = JarvisVS.Idle
        }
    }

    fun handleAudioPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            _viewState.value = JarvisVS.Idle
        } else {
            _viewState.value = JarvisVS.Error("You must give the permission to use ai")
        }
    }
}