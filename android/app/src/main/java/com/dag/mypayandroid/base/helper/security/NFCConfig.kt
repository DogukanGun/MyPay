package com.dag.mypayandroid.base.helper.security

object NFCConfig {

    const val CUSTOM_MIME_TYPE = "application/vnd.dag.mypayandroid.payment"

    /**
     * App identifier - used in intent filters and debugging
     * Change this to your actual app's package name or unique identifier
     */
    const val APP_IDENTIFIER = "com.dag.mypayandroid"

    /**
     * Message types for JSON communication
     */
    const val MESSAGE_TYPE_PAYMENT_REQUEST = "payment_request"
    const val MESSAGE_TYPE_PAYMENT_RESPONSE = "payment_response"
    const val MESSAGE_TYPE_KEY = "type"

    /**
     * Payment status constants
     */
    const val PAYMENT_TX = "tx"
    const val PAYMENT_URI = "uri"

    /**
     * Request/Transaction ID prefixes
     */
    const val REQUEST_ID_PREFIX = "REQ"
    const val TRANSACTION_ID_PREFIX = "TXN"

    /**
     * Timeout values (in milliseconds)
     */
    const val NFC_TIMEOUT = 30000L // 30 seconds
    const val RESPONSE_TIMEOUT = 60000L // 60 seconds

    /**
     * Supported currencies
     */
    val SUPPORTED_CURRENCIES = listOf("USD", "EUR", "GBP", "CAD", "AUD")

    /**
     * Maximum payment amount (to prevent errors)
     */
    const val MAX_PAYMENT_AMOUNT = 10000.0

    /**
     * Generate your custom MIME type
     * Call this method to customize the MIME type for your specific app
     */
    fun generateCustomMimeType(company: String, appName: String, dataType: String = "payment"): String {
        val cleanCompany = company.lowercase().replace(" ", "").replace("-", "")
        val cleanAppName = appName.lowercase().replace(" ", "").replace("-", "")
        val cleanDataType = dataType.lowercase().replace(" ", "").replace("-", "")
        return "application/vnd.$cleanCompany.$cleanAppName.$cleanDataType"
    }

    /**
     * Validate MIME type format
     */
    fun isValidMimeType(mimeType: String): Boolean {
        val pattern = Regex("^application/vnd\\.[a-z0-9]+\\.[a-z0-9]+\\.[a-z0-9]+$")
        return pattern.matches(mimeType)
    }
}
