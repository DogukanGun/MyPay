package com.dag.mypayandroid.feature.jarvis.presentation

import com.dag.mypayandroid.base.BaseVS
import java.io.File


sealed class JarvisVS: BaseVS {
    data object Welcome: JarvisVS()
    object Idle : JarvisVS()
    object AskPermission : JarvisVS()
    object Listening : JarvisVS()
    object Processing : JarvisVS()
    data class Ready(val audioFile: File, val audioData: ByteArray) : JarvisVS()
    data class Error(val message: String) : JarvisVS()
}