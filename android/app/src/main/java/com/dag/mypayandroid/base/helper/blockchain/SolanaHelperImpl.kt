package com.dag.mypayandroid.base.helper.blockchain

import NFCHelper
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.solanapay.SolanaPayURLEncoder
import com.dag.mypayandroid.base.solanapay.SolanaPayURLParser
import com.dag.mypayandroid.base.solanapay.TransferCreator
import com.dag.mypayandroid.base.solanapay.TransferRequestURLFields
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sol4k.Base58
import org.sol4k.Connection
import org.sol4k.Keypair
import org.sol4k.PublicKey
import org.sol4k.Transaction
import org.sol4k.instruction.TransferInstruction
import java.math.BigDecimal
import java.net.URL

class SolanaHelperImpl(
    val connection: Connection,
    val walletManager: WalletManager
) : SolanaHelper {

    private fun makePayment(
        keypair: Keypair,
        transferRequestField: TransferRequestURLFields,
        onSigned: (tx: Transaction)-> Unit
    ) {
        walletManager.getPublicKey() ?: return
        walletManager.getPublicKey()?.let {
            val tx = TransferCreator
                .createTransfer(
                    connection = connection,
                    sender = PublicKey(it),
                    fields = transferRequestField
                )
            tx.sign(keypair)
            onSigned(tx)
        }
    }

    override fun prepareSolanaPay(
        transferRequestField: TransferRequestURLFields,
        onUrlReady: (tx: URL)-> Unit
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
            makePayment(
                keypair,
                TransferRequestURLFields(
                    recipient = tx.recipient,
                    amount = tx.amount,
                    tokenDecimal = tx.tokenDecimal,
                    splToken = tx.splToken,
                ),
                onSigned = onSigned
            )
        }
    }

}