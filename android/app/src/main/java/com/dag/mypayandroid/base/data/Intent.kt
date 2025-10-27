package com.dag.mypayandroid.base.data


sealed class Intent {
    class Logout(): Intent()
    class WalletManagement(): Intent()
}