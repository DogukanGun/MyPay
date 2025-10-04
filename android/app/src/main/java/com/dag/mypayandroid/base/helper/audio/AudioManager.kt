package com.dag.mypayandroid.base.helper.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    
    private val _recordingState = MutableStateFlow<AudioRecordingState>(AudioRecordingState.Idle)
    val recordingState: StateFlow<AudioRecordingState> = _recordingState.asStateFlow()
    
    private val _audioData = MutableStateFlow<ByteArray?>(null)
    val audioData: StateFlow<ByteArray?> = _audioData.asStateFlow()
    
    companion object {
        private const val TAG = "AudioManager"
        private const val AUDIO_FILE_NAME = "jarvis_recording.3gp"
    }
    
    sealed class AudioRecordingState {
        object Idle : AudioRecordingState()
        object Recording : AudioRecordingState()
        object Processing : AudioRecordingState()
        data class Ready(val audioFile: File, val audioData: ByteArray) : AudioRecordingState()
        data class Error(val message: String) : AudioRecordingState()
    }
    
    fun startRecording(): Boolean {
        if (!hasAudioPermission()) {
            _recordingState.value = AudioRecordingState.Error("Audio permission not granted")
            return false
        }
        
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return false
        }
        
        try {
            // Create audio file
            audioFile = File(context.cacheDir, AUDIO_FILE_NAME)
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFile!!.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                
                prepare()
                start()
            }
            
            isRecording = true
            _recordingState.value = AudioRecordingState.Recording
            Log.d(TAG, "Recording started")
            return true
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start recording", e)
            _recordingState.value = AudioRecordingState.Error("Failed to start recording: ${e.message}")
            cleanup()
            return false
        }
    }
    
    fun stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "Not currently recording")
            return
        }
        
        try {
            _recordingState.value = AudioRecordingState.Processing
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            audioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    // Read audio file to byte array
                    val audioBytes = file.readBytes()
                    _audioData.value = audioBytes
                    _recordingState.value = AudioRecordingState.Ready(file, audioBytes)
                    Log.d(TAG, "Recording stopped, file size: ${audioBytes.size} bytes")
                } else {
                    _recordingState.value = AudioRecordingState.Error("Audio file is empty or doesn't exist")
                }
            } ?: run {
                _recordingState.value = AudioRecordingState.Error("Audio file is null")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            _recordingState.value = AudioRecordingState.Error("Failed to stop recording: ${e.message}")
            cleanup()
        }
    }
    
    fun getAudioFileForBackend(): File? {
        return when (val state = _recordingState.value) {
            is AudioRecordingState.Ready -> state.audioFile
            else -> null
        }
    }
    
    fun getAudioDataForBackend(): ByteArray? {
        return when (val state = _recordingState.value) {
            is AudioRecordingState.Ready -> state.audioData
            else -> null
        }
    }
    
    fun clearAudioData() {
        _audioData.value = null
        _recordingState.value = AudioRecordingState.Idle
        cleanup()
    }
    
    private fun cleanup() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            audioFile?.delete()
            audioFile = null
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
    
    fun isCurrentlyRecording(): Boolean = isRecording
}