package com.dag.mypayandroid.base.helper

import com.dag.mypayandroid.base.data.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class IntentManager @Inject constructor(
    private val activityHolder: ActivityHolder
) {
    private val _intentFlow = MutableSharedFlow<Intent>(replay = 0)
    val intent: SharedFlow<Intent> = _intentFlow

    suspend fun requestIntent(intent: Intent) {
        _intentFlow.emit(intent)
    }

    private suspend fun executeIntent() {
        _intentFlow.collect {
            when (it) {
                is Intent.Web3AuthLogout -> {
                    it.web3Auth.logout().await()
                }
            }
        }
    }

}