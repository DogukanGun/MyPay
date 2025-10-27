package com.dag.mypayandroid.base.helper.system

import com.dag.mypayandroid.base.data.Intent
import kotlinx.coroutines.future.await
import org.sol4k.RpcUrl
import javax.inject.Inject

class IntentManager @Inject constructor(
    private val activityHolder: ActivityHolder
) {
    suspend fun requestIntent(intent: Intent) {
        executeIntent(intent)
    }

    private suspend fun executeIntent(intent: Intent) {
        when (intent) {
            is Intent.Logout -> {
                // Handle logout - clear stored tokens and navigate to login
            }
            is Intent.WalletManagement -> {
                // Handle wallet management
            }
        }
    }

}