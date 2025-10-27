package com.dag.mypayandroid.feature.jarvis.presentation

import com.dag.mypayandroid.base.BaseVS
import java.io.File


sealed class JarvisVS: BaseVS {
    object Idle : JarvisVS()
    object AskPermission : JarvisVS()
    object Listening : JarvisVS()
    data class Confirming(val transcription: String) : JarvisVS()
    object Processing : JarvisVS()
    data class Success(val message: String) : JarvisVS()
    data class Error(val message: String) : JarvisVS()
}