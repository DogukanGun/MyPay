package com.dag.mypayandroid.base.data

import com.web3auth.core.Web3Auth

sealed class Intent {
    class Web3AuthLogout(val web3Auth: Web3Auth): Intent()
    class Web3WalletManagement(val web3Auth: Web3Auth): Intent()
}