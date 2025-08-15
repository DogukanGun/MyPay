package com.dag.mypayandroid.base.helper
import org.sol4k.Keypair
import org.sol4k.PublicKey

interface SolanaHelper {
    suspend fun getBalance(publicKey: PublicKey): String
    suspend fun signAndSendSol(sender: Keypair): String

    suspend fun signSendSol(sender: Keypair): String
}