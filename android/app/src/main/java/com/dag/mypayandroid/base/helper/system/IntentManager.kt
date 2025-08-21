package com.dag.mypayandroid.base.helper.system

import com.dag.mypayandroid.base.data.Intent
import com.web3auth.core.types.ChainConfig
import com.web3auth.core.types.ChainNamespace
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
            is Intent.Web3AuthLogout -> {
                intent.web3Auth.logout().await()
            }
            is Intent.Web3WalletManagement -> {
                intent.web3Auth.launchWalletServices(
                    ChainConfig(
                        chainNamespace = ChainNamespace.SOLANA,
                        chainId = "0x2",
                        rpcTarget = RpcUrl.DEVNET.value,
                    )
                )
            }
        }
    }

}