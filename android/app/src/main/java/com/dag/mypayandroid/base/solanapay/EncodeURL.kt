package com.dag.mypayandroid.base.solanapay

import com.dag.mypayandroid.base.solanapay.SolanaPayConstants.SOLANA_PROTOCOL
import java.net.URI
import java.net.URL
import java.net.URLEncoder

object SolanaPayURLEncoder {

    fun encodeURL(fields: TransferRequestURLFields): URI {
        return encodeTransferRequestURL(fields)
    }
    private fun encodeTransferRequestURL(fields: TransferRequestURLFields): URI {
        val pathname = fields.recipient.toBase58()
        val baseUri = "$SOLANA_PROTOCOL$pathname" // e.g. "solana:"

        val queryParams = mutableListOf<String>()
        queryParams.add("amount=${fields.amount.toPlainString()}")
        fields.splToken?.let { queryParams.add("spl-token=${it.toBase58()}") }
        fields.reference?.forEach { queryParams.add("reference=${it.toBase58()}") }
        fields.label?.let { queryParams.add("label=${URLEncoder.encode(it, "UTF-8")}") }
        fields.message?.let { queryParams.add("message=${URLEncoder.encode(it, "UTF-8")}") }
        fields.memo?.let { queryParams.add("memo=${URLEncoder.encode(it, "UTF-8")}") }

        val uriString = baseUri + if (queryParams.isNotEmpty()) {
            "?${queryParams.joinToString("&")}"
        } else ""

        return URI(uriString)
    }
} 