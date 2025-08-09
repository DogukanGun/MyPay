package com.dag.mypayandroid.base.helper

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.EthereumState
import io.metamask.androidsdk.SDKOptions
import io.metamask.androidsdk.Result
import kotlin.apply

interface WalletManagement {
    fun connect(callback: ((Result) -> Unit)?)
    fun sendRequest(request: EthereumRequest, callback: ((Result) -> Unit)?)
    fun signMessage(message: String, callback: ((Result) -> Unit)?)
    fun getSelectedAddress(): String
    fun isConnected(): Boolean
}

class WalletManagementImpl(
    context: Context
): WalletManagement {
    val appMetadata = DappMetadata("CarbonMapper", "https://www.carbonmapper.com")
    
    // Hedera Testnet configuration
    companion object {
    }
    
    val ethereum = Ethereum(context, appMetadata, SDKOptions(
        readonlyRPCMap = mapOf(),
        infuraAPIKey = null
    ))

    val ethereumState = MediatorLiveData<EthereumState>().apply {
        addSource(ethereum.ethereumState) { newEthereumState ->
            value = newEthereumState
        }
    }

    override fun connect(callback: ((Result) -> Unit)?) {
        ethereum.connect(callback)
    }

    override fun sendRequest(request: EthereumRequest, callback: ((Result) -> Unit)?) {
        ethereum.sendRequest(request) {
            callback?.invoke(it)
        }
    }

    override fun signMessage(message: String, callback: ((Result) -> Unit)?) {
        ethereum.connectSign(message) {
            callback?.invoke(it)
        }
    }

    override fun getSelectedAddress(): String {
        return ethereum.selectedAddress
    }

    override fun isConnected(): Boolean {
        return ethereumState.value?.selectedAddress != null
    }

}