package com.dag.mypayandroid.feature.jarvis.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.helper.audio.AudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class JarvisVM @Inject constructor(
    val processHandler: ProcessHandler,
    private val audioManager: AudioManager
): BaseVM<JarvisVS>(JarvisVS.Welcome) {

    
    val isListening: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            _viewState.collect { state ->
                value = state is JarvisVS.Listening
            }
        }
    }.asStateFlow()
    
    init {
        // Observe audio manager state changes
        viewModelScope.launch {
            audioManager.recordingState.collect { audioState ->
                when (audioState) {
                    is AudioManager.AudioRecordingState.Idle -> _viewState.value = JarvisVS.Idle
                    is AudioManager.AudioRecordingState.Recording -> _viewState.value = JarvisVS.Listening
                    is AudioManager.AudioRecordingState.Processing -> _viewState.value = JarvisVS.Processing
                    is AudioManager.AudioRecordingState.Ready -> {
                        _viewState.value = JarvisVS.Ready(audioState.audioFile, audioState.audioData)
                    }
                    is AudioManager.AudioRecordingState.Error -> {
                        _viewState.value = JarvisVS.Error(audioState.message)
                    }
                }
            }
        }
    }
    
    fun startListening() {
        if (audioManager.startRecording()) {
            _viewState.value = JarvisVS.Listening
        }
    }
    
    fun stopListening() {
        audioManager.stopRecording()
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
    
    fun getAudioFileForBackend(): File? {
        return audioManager.getAudioFileForBackend()
    }
    
    fun getAudioDataForBackend(): ByteArray? {
        return audioManager.getAudioDataForBackend()
    }
    
    fun clearAudioData() {
        audioManager.clearAudioData()
        _viewState.value = JarvisVS.Idle
    }
    
    fun sendToBackend() {
        val audioFile = getAudioFileForBackend()
        val audioData = getAudioDataForBackend()
        
        if (audioFile != null && audioData != null) {
            processHandler.show("Sending to backend...")
            // TODO: Implement actual backend call here
            // backendService.sendAudio(audioFile, audioData)
            
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                processHandler.show("Processing your request...", duration = 3000L)
                kotlinx.coroutines.delay(3500)
                clearAudioData()
            }
        }
    }

    fun checkPermission() {
        val isPermissionGranted = audioManager.hasAudioPermission()
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