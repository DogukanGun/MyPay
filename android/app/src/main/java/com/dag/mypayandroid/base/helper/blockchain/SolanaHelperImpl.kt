package com.dag.mypayandroid.base.helper.blockchain

import com.dag.mypayandroid.base.solanapay.SolanaPayURLEncoder
import com.dag.mypayandroid.base.solanapay.SolanaPayURLParser
import com.dag.mypayandroid.base.solanapay.TransferCreator
import com.dag.mypayandroid.base.solanapay.TransferRequestURLFields
import org.sol4k.Connection
import org.sol4k.Keypair
import org.sol4k.PublicKey
import org.sol4k.Transaction
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SolanaHelperImpl(
    val connection: Connection,
    val walletManager: WalletManager
) : SolanaHelper {

    override fun prepareSolanaPay(
        transferRequestField: TransferRequestURLFields,
        onUrlReady: (tx: URI)-> Unit
    ) {
        onUrlReady(SolanaPayURLEncoder.encodeURL(transferRequestField))
    }

    override suspend fun receiveSolanaPayAndMakePayment(
        keypair: Keypair,
        paymentUrl: String,
        onSigned: (Transaction) -> Unit
    ) {
        val tx = SolanaPayURLParser.parseURL(paymentUrl)
        if (tx is TransferRequestURLFields) {
            try {
                // Use the already provided keypair (which was created from stored wallet data)
                // Move network operations to IO thread
                val transaction = withContext(Dispatchers.IO) {
                    TransferCreator.createTransfer(
                        connection = connection,
                        sender = keypair.publicKey,
                        fields = TransferRequestURLFields(
                            recipient = tx.recipient,
                            amount = tx.amount,
                            tokenDecimal = tx.tokenDecimal,
                            splToken = tx.splToken,
                        )
                    )
                }
                transaction.sign(keypair)
                onSigned(transaction)
            } catch (e: Exception) {
                throw Exception("Failed to create transaction: ${e.message}")
            }
        }
    }

}