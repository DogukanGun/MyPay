package com.dag.mypayandroid.base.solanapay

import com.dag.mypayandroid.base.solanapay.SolanaPayConstants.SOLANA_PROTOCOL
import java.net.URL
import java.net.URLEncoder

object URLEncoder {
    fun encodeURL(fields: TransactionRequestURLFields): URL {
        return encodeTransactionRequestURL(fields)
    }

    fun encodeURL(fields: TransferRequestURLFields): URL {
        return encodeTransferRequestURL(fields)
    }

    private fun encodeTransactionRequestURL(fields: TransactionRequestURLFields): URL {
        val pathname = if (fields.link.query.isNullOrEmpty()) {
            fields.link.toString().removeSuffix("/")
        } else {
            URLEncoder.encode(fields.link.toString().replace("/\\?", "?"), "UTF-8")
        }

        val url = URL("$SOLANA_PROTOCOL$pathname")
        val urlBuilder = url.toURI().toURL().toString()

        val queryParams = mutableListOf<String>()
        fields.label?.let { queryParams.add("label=${URLEncoder.encode(it, "UTF-8")}") }
        fields.message?.let { queryParams.add("message=${URLEncoder.encode(it, "UTF-8")}") }

        return URL(urlBuilder + (if (queryParams.isNotEmpty()) "?${queryParams.joinToString("&")}" else ""))
    }

    private fun encodeTransferRequestURL(fields: TransferRequestURLFields): URL {
        val pathname = fields.recipient.toBase58()
        val url = URL("$SOLANA_PROTOCOL$pathname")
        val urlBuilder = url.toURI().toURL().toString()

        val queryParams = mutableListOf<String>()
        queryParams.add("amount=${fields.amount.toPlainString()}")
        fields.splToken?.let { queryParams.add("spl-token=${it.toBase58()}") }
        fields.reference?.forEach { queryParams.add("reference=${it.toBase58()}") }
        fields.label?.let { queryParams.add("label=${URLEncoder.encode(it, "UTF-8")}") }
        fields.message?.let { queryParams.add("message=${URLEncoder.encode(it, "UTF-8")}") }
        fields.memo?.let { queryParams.add("memo=${URLEncoder.encode(it, "UTF-8")}") }

        return URL(urlBuilder + (if (queryParams.isNotEmpty()) "?${queryParams.joinToString("&")}" else ""))
    }
} 