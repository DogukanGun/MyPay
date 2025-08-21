package com.dag.mypayandroid.base.solanapay

import org.sol4k.PublicKey
import java.math.BigDecimal
import java.net.URL

typealias Recipient = PublicKey
typealias Amount = BigDecimal
typealias SPLToken = PublicKey
typealias Reference = PublicKey
typealias References = List<Reference>
typealias Label = String
typealias Message = String
typealias Memo = String
typealias Link = URL

data class TransactionRequestURLFields(
    val link: Link,
    val label: Label? = null,
    val message: Message? = null
)

data class TransferRequestURLFields(
    val recipient: Recipient,
    val amount: Amount,
    val tokenDecimal: Int,
    val splToken: SPLToken? = null,
    val reference: References? = null,
    val label: Label? = null,
    val message: Message? = null,
    val memo: Memo? = null
) 