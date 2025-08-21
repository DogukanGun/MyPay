package com.dag.mypayandroid.base.solanapay

import com.dag.mypayandroid.base.solanapay.SolanaPayConstants.HTTPS_PROTOCOL
import com.dag.mypayandroid.base.solanapay.SolanaPayConstants.SOLANA_PROTOCOL
import org.sol4k.PublicKey
import java.math.BigDecimal
import java.net.URL
import java.net.URLDecoder

class ParseURLError(message: String) : Exception(message)

object URLParser {
    fun parseURL(url: String): Any {
        val parsedUrl = if (url.length > 2048) {
            throw ParseURLError("URL length invalid")
        } else {
            URL(url)
        }

        if (parsedUrl.protocol != SOLANA_PROTOCOL) {
            throw ParseURLError("Protocol invalid")
        }

        if (parsedUrl.path.isNullOrEmpty()) {
            throw ParseURLError("Pathname missing")
        }

        return if (parsedUrl.path.contains("[:%]".toRegex())) {
            parseTransactionRequestURL(parsedUrl)
        } else {
            parseTransferRequestURL(parsedUrl)
        }
    }

    private fun parseTransactionRequestURL(url: URL): TransactionRequestURLFields {
        val link = URL(url.path.removePrefix("/").let { URLDecoder.decode(it, "UTF-8") })
        
        if (link.protocol != HTTPS_PROTOCOL) {
            throw ParseURLError("Link invalid")
        }

        return TransactionRequestURLFields(
            link = link,
            label = url.getQueryParameter("label"),
            message = url.getQueryParameter("message")
        )
    }

    private fun parseTransferRequestURL(url: URL): TransferRequestURLFields {
        val recipient = try {
            PublicKey(url.path.removePrefix("/"))
        } catch (e: Exception) {
            throw ParseURLError("Recipient invalid")
        }

        val amount = url.getQueryParameter("amount")?.let { amountParam ->
            if (!amountParam.matches("^\\d+(\\.\\d+)?\$".toRegex())) {
                throw ParseURLError("Amount invalid")
            }

            val parsedAmount = BigDecimal(amountParam)
            if ( parsedAmount <= BigDecimal.ZERO) {
                throw ParseURLError("Amount invalid")
            }
            parsedAmount
        }
        if (amount == null) {
            throw IllegalStateException()
        }

        val splToken = url.getQueryParameter("spl-token")?.let { splTokenParam ->
            try {
                PublicKey(splTokenParam)
            } catch (e: Exception) {
                throw ParseURLError("SPL Token invalid")
            }
        }

        val references = url.getQueryParameters("reference")?.map { referenceParam ->
            try {
                PublicKey(referenceParam)
            } catch (e: Exception) {
                throw ParseURLError("Reference invalid")
            }
        }

        return TransferRequestURLFields(
            recipient = recipient,
            amount = amount,
            splToken = splToken,
            reference = references,
            label = url.getQueryParameter("label"),
            message = url.getQueryParameter("message"),
            memo = url.getQueryParameter("memo"),
            tokenDecimal = getTokenDecimal(splToken)
        )
    }

    // Extension function to get query parameters
    private fun URL.getQueryParameter(key: String): String? {
        return query?.split("&")
            ?.map { it.split("=") }
            ?.firstOrNull { it.size == 2 && it[0] == key }
            ?.get(1)
            ?.let { URLDecoder.decode(it, "UTF-8") }
    }

    // Extension function to get multiple query parameters
    private fun URL.getQueryParameters(key: String): List<String>? {
        return query?.split("&")
            ?.filter { it.startsWith("$key=") }
            ?.map { it.substringAfter("=") }
            ?.map { URLDecoder.decode(it, "UTF-8") }
            ?.takeIf { it.isNotEmpty() }
    }

    private fun getTokenDecimal(mint: PublicKey?): Int{
        return 9
    }
} 