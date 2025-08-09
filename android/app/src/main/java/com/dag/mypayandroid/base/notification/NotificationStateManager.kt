package com.dag.mypayandroid.base.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationStateManager @Inject constructor() {
    private val _shouldShowPopup = MutableStateFlow(false)
    val shouldShowPopup: StateFlow<Boolean> = _shouldShowPopup.asStateFlow()

    fun setShowPopup(show: Boolean) {
        _shouldShowPopup.value = show
    }
}
