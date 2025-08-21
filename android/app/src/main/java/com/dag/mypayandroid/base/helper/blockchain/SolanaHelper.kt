package com.dag.mypayandroid.base.helper.blockchain
import com.dag.mypayandroid.base.solanapay.TransferRequestURLFields
import org.sol4k.Keypair
import org.sol4k.PublicKey
import org.sol4k.Transaction
import java.net.URL

interface SolanaHelper {
    fun prepareSolanaPay(
        transferRequestField: TransferRequestURLFields,
        onUrlReady: (tx: URL)-> Unit
    )
    suspend fun receiveSolanaPayAndMakePayment(
        keypair: Keypair,
        paymentUrl: String,
        onSigned: (tx: Transaction)-> Unit
    )
}