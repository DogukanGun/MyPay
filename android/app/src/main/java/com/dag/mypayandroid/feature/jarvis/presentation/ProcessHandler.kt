package com.dag.mypayandroid.feature.jarvis.presentation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.dag.mypayandroid.feature.jarvis.presentation.components.StatusCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class ProcessHandler @Inject constructor() {
    
    private val _currentCard = MutableStateFlow<StatusCardData?>(null)
    val currentCard: StateFlow<StatusCardData?> = _currentCard.asStateFlow()
    
    fun show(
        message: String,
        duration: Long? = null,
        condition: (() -> Boolean)? = null,
        backgroundColor: Color? = null
    ) {
        val cardData = StatusCardData(
            message = message,
            duration = duration,
            condition = condition,
            backgroundColor = backgroundColor
        )
        _currentCard.value = cardData
    }
    
    fun showWithDuration(
        message: String,
        durationMillis: Long,
        backgroundColor: Color? = null
    ) {
        show(
            message = message,
            duration = durationMillis,
            backgroundColor = backgroundColor
        )
    }
    
    fun showWithCondition(
        message: String,
        condition: () -> Boolean,
        backgroundColor: Color? = null
    ) {
        show(
            message = message,
            condition = condition,
            backgroundColor = backgroundColor
        )
    }
    
    fun hide() {
        _currentCard.value = null
    }
    
    fun isShowing(): Boolean {
        return _currentCard.value != null
    }
}