package com.dag.mypayandroid.base.solanapay

import com.dag.mypayandroid.base.solanapay.SolanaPayConstants.HTTPS_PROTOCOL
import com.dag.mypayandroid.base.solanapay.SolanaPayConstants.SOLANA_PROTOCOL
import org.sol4k.PublicKey
import java.math.BigDecimal
import java.net.URI
import java.net.URL
import java.net.URLDecoder

class ParseURLError(message: String) : Exception(message)

object SolanaPayURLParser {
    fun parseURL(url: String): TransferRequestURLFields {
        val parsedUri = if (url.length > 2048) {
            throw ParseURLError("URL length invalid")
        } else {
            URI(url) // ✅ Use URI, supports custom protocols
        }

        if (parsedUri.scheme != SOLANA_PROTOCOL) {
            throw ParseURLError("Protocol invalid")
        }

        if (parsedUri.path.isNullOrEmpty()) {
            throw ParseURLError("Pathname missing")
        }

        return parseTransferRequestURL(parsedUri)
    }

    private fun parseTransferRequestURL(uri: URI): TransferRequestURLFields {
        val recipient = try {
            PublicKey(uri.path.removePrefix("/"))
        } catch (e: Exception) {
            throw ParseURLError("Recipient invalid")
        }

        val amount = uri.getQueryParameter("amount")?.let { amountParam ->
            if (!amountParam.matches("^\\d+(\\.\\d+)?\$".toRegex())) {
                throw ParseURLError("Amount invalid")
            }

            val parsedAmount = BigDecimal(amountParam)
            if (parsedAmount <= BigDecimal.ZERO) {
                throw ParseURLError("Amount invalid")
            }
            parsedAmount
        } ?: throw ParseURLError("Amount missing")

        val splToken = uri.getQueryParameter("spl-token")?.let { splTokenParam ->
            try {
                PublicKey(splTokenParam)
            } catch (e: Exception) {
                throw ParseURLError("SPL Token invalid")
            }
        }

        val references = uri.getQueryParameters("reference")?.map { referenceParam ->
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
            label = uri.getQueryParameter("label"),
            message = uri.getQueryParameter("message"),
            memo = uri.getQueryParameter("memo"),
            tokenDecimal = getTokenDecimal(splToken)
        )
    }

    // Extension function to get query parameters
    private fun URI.getQueryParameter(key: String): String? {
        return this.query?.split("&")
            ?.map { it.split("=") }
            ?.firstOrNull { it.size == 2 && it[0] == key }
            ?.get(1)
            ?.let { URLDecoder.decode(it, "UTF-8") }
    }

    // Extension function to get multiple query parameters
    private fun URI.getQueryParameters(key: String): List<String>? {
        return this.query?.split("&")
            ?.filter { it.startsWith("$key=") }
            ?.map { it.substringAfter("=") }
            ?.map { URLDecoder.decode(it, "UTF-8") }
            ?.takeIf { it.isNotEmpty() }
    }

    private fun getTokenDecimal(mint: PublicKey?): Int {
        return 9
    }
}