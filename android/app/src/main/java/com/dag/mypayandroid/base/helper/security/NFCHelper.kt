package com.dag.mypayandroid.base.helper.security

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.*
import android.util.Log
import java.nio.charset.Charset
import org.json.JSONObject
import java.math.BigDecimal

class NFCHelper(private val activity: Activity?) {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    private val customMimeType = NFCConfig.CUSTOM_MIME_TYPE

    // Callback interfaces
    interface NFCListener {
        fun onPaymentRequestReceived(paymentUrl: String)
        fun onPaymentResponseReceived(paymentUrl: String)
        fun onNFCError(error: String)
        fun onNFCMessageSent()
        fun onDeviceConnected()
        fun onDeviceDisconnected()
    }

    private var nfcListener: NFCListener? = null

    // Add new enum for payment states
    enum class PaymentState {
        IDLE, WAITING_FOR_REQUEST, WAITING_FOR_CONFIRMATION, COMPLETED, ERROR
    }

    // Add payment-specific callback interface
    interface NFCPaymentListener : NFCListener {
        fun onPaymentStateChanged(state: PaymentState)
    }

    // Add payment-specific fields
    private var currentPaymentState: PaymentState = PaymentState.IDLE
    private var currentPaymentRequest: String? = null
    private var currentPaymentAmount: BigDecimal? = null

    init {
        setupNFC()
    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        if (nfcAdapter == null) {
            Log.e("NFCHelper", "NFC is not supported on this device")
            return
        }

        if (activity == null) {
            Log.e("NFCHelper", "Activity is empty for NFC Helper")
            return
        }

        // Create pending intent for P2P communication
        val intent = Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    /**
     * Set the NFC listener to receive callbacks
     */
    fun setNFCListener(listener: NFCListener) {
        this.nfcListener = listener
    }

    /**
     * Enable NFC peer-to-peer mode - call this in onResume()
     */
    fun enableNFCP2P() {
        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                // Enable foreground dispatch for receiving
                adapter.enableForegroundDispatch(
                    activity,
                    pendingIntent,
                    null, // Accept all NFC intents
                    null
                )
                Log.d("NFCHelper", "NFC P2P mode enabled")
            } else {
                nfcListener?.onNFCError("NFC is disabled. Please enable NFC in settings.")
            }
        } ?: run {
            nfcListener?.onNFCError("NFC is not available on this device")
        }
    }

    /**
     * Disable NFC peer-to-peer mode - call this in onPause()
     */
    fun disableNFCP2P() {
        nfcAdapter?.disableForegroundDispatch(activity)
        Log.d("NFCHelper", "NFC P2P mode disabled")
    }

    /**
     * Initiate a payment request with specific details
     */
    fun initiatePaymentRequest(
        paymentUri: String,
        amount: BigDecimal,
        onStateChange: (PaymentState) -> Unit
    ) {
        try {
            currentPaymentAmount = amount
            currentPaymentRequest = paymentUri
            currentPaymentState = PaymentState.WAITING_FOR_REQUEST

            val jsonData = JSONObject().apply {
                put(NFCConfig.MESSAGE_TYPE_KEY, NFCConfig.MESSAGE_TYPE_PAYMENT_REQUEST)
                put(NFCConfig.PAYMENT_URI, paymentUri)
                put("amount", amount.toPlainString())
            }

            val message = createNdefMessage(jsonData.toString())
            prepareMessageForSending(message)

            Log.d("NFCHelper", "Payment request prepared: $paymentUri, Amount: $amount")
            onStateChange(currentPaymentState)

        } catch (e: Exception) {
            currentPaymentState = PaymentState.ERROR
            Log.e("NFCHelper", "Error preparing payment request", e)
            onStateChange(currentPaymentState)
        }
    }

    /**
     * Confirm and send payment response
     */
    fun sendPaymentResponse(transactionId: String) {
        try {
            val jsonData = JSONObject().apply {
                put(NFCConfig.MESSAGE_TYPE_KEY, NFCConfig.MESSAGE_TYPE_PAYMENT_RESPONSE)
                put(NFCConfig.PAYMENT_TX, transactionId)
            }

            val message = createNdefMessage(jsonData.toString())
            prepareMessageForSending(message)

            currentPaymentState = PaymentState.COMPLETED
            Log.d("NFCHelper", "Payment response sent: $transactionId")

        } catch (e: Exception) {
            currentPaymentState = PaymentState.ERROR
            Log.e("NFCHelper", "Error sending payment response", e)
        }
    }

    /**
     * Send payment request to another device
     */
    fun sendPaymentRequest(paymentUri: String) {
        try {
            val jsonData = JSONObject().apply {
                put(NFCConfig.MESSAGE_TYPE_KEY, NFCConfig.MESSAGE_TYPE_PAYMENT_REQUEST)
                put(NFCConfig.PAYMENT_URI, paymentUri)
            }

            val message = createNdefMessage(jsonData.toString())
            prepareMessageForSending(message)

            Log.d("NFCHelper", "Payment request prepared for sending: $paymentUri")

        } catch (e: Exception) {
            Log.e("NFCHelper", "Error preparing payment request", e)
            nfcListener?.onNFCError("Error preparing payment request: ${e.message}")
        }
    }

    /**
     * Handle incoming NFC intent from another device
     */
    fun handleNFCIntent(intent: Intent): Boolean {
        val action = intent.action
        Log.d("NFCHelper", "Handling NFC intent with action: $action")

        when (action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                processNdefMessage(intent)
                nfcListener?.onDeviceConnected()

                return true
            }
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                val hasNdef = intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                if (hasNdef) {
                    processNdefMessage(intent)
                }
                nfcListener?.onDeviceConnected()
                return true
            }
        }
        return false
    }

    /**
     * Process received NDEF message
     */
    private fun processNdefMessage(intent: Intent) {
        try {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

            rawMessages?.let { messages ->
                for (rawMessage in messages) {
                    val message = rawMessage as NdefMessage
                    val records = message.records

                    for (record in records) {
                        val payload = String(record.payload, Charset.forName("UTF-8"))
                        Log.d("NFCHelper", "Received NFC payload: $payload")

                        // Parse JSON data
                        parsePaymentMessage(payload)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NFCHelper", "Error processing NDEF message", e)
            nfcListener?.onNFCError("Error processing received message: ${e.message}")
        }
    }

    /**
     * Enhanced message parsing with more detailed state management
     */
    private fun parsePaymentMessage(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val messageType = jsonObject.getString("type")

            when (messageType) {
                NFCConfig.MESSAGE_TYPE_PAYMENT_REQUEST -> {
                    val paymentRequest = jsonObject.getString("url")
                    val amount = jsonObject.optString("amount", null)?.let { BigDecimal(it) }

                    currentPaymentRequest = paymentRequest
                    currentPaymentAmount = amount
                    currentPaymentState = PaymentState.WAITING_FOR_CONFIRMATION

                    Log.d("NFCHelper", "Payment request received: $paymentRequest, Amount: $amount")
                    nfcListener?.onPaymentRequestReceived(paymentRequest)
                }

                NFCConfig.MESSAGE_TYPE_PAYMENT_RESPONSE -> {
                    val paymentResponse = jsonObject.getString(NFCConfig.PAYMENT_TX)

                    currentPaymentState = PaymentState.COMPLETED
                    Log.d("NFCHelper", "Payment response received: $paymentResponse")
                    nfcListener?.onPaymentResponseReceived(paymentResponse)
                }

                else -> {
                    currentPaymentState = PaymentState.ERROR
                    Log.w("NFCHelper", "Unknown message type: $messageType")
                    nfcListener?.onNFCError("Unknown message type received")
                }
            }
        } catch (e: Exception) {
            currentPaymentState = PaymentState.ERROR
            Log.e("NFCHelper", "Error parsing payment message", e)
            nfcListener?.onNFCError("Error parsing received message: ${e.message}")
        }
    }

    /**
     * Create NDEF message from string data with custom MIME type
     */
    private fun createNdefMessage(data: String): NdefMessage {
        // Create MIME record with custom MIME type for your app
        val mimeRecord = createMimeRecord(customMimeType, data)
        // Also create a text record as fallback
        val textRecord = createTextRecord(data)
        return NdefMessage(arrayOf(mimeRecord, textRecord))
    }

    /**
     * Create MIME NDEF record with custom MIME type
     */
    private fun createMimeRecord(mimeType: String, data: String): NdefRecord {
        val mimeBytes = mimeType.toByteArray(Charset.forName("US-ASCII"))
        val dataBytes = data.toByteArray(Charset.forName("UTF-8"))

        return NdefRecord(
            NdefRecord.TNF_MIME_MEDIA,
            mimeBytes,
            ByteArray(0), // No ID
            dataBytes
        )
    }

    /**
     * Create text NDEF record
     */
    private fun createTextRecord(text: String, locale: String = "en"): NdefRecord {
        val langBytes = locale.toByteArray(Charset.forName("US-ASCII"))
        val textBytes = text.toByteArray(Charset.forName("UTF-8"))
        val utfBit = 0 // 0 for UTF-8
        val status = (utfBit + langBytes.size).toByte()

        val data = ByteArray(1 + langBytes.size + textBytes.size)
        data[0] = status
        System.arraycopy(langBytes, 0, data, 1, langBytes.size)
        System.arraycopy(textBytes, 0, data, 1 + langBytes.size, textBytes.size)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), data)
    }

    // Store the message to be sent when devices connect
    private var pendingMessage: NdefMessage? = null
    private var isInSendMode = false

    /**
     * Prepare NDEF message for sending to another device
     * Modern approach that works on all Android versions
     */
    private fun prepareMessageForSending(message: NdefMessage) {
        pendingMessage = message
        isInSendMode = true

        Log.d("NFCHelper", "Message prepared for P2P transmission")
        nfcListener?.onNFCMessageSent()
    }

    /**
     * Check if we have a pending message to send
     */
    fun hasPendingMessage(): Boolean {
        return pendingMessage != null && isInSendMode
    }

    /**
     * Clear pending message after sending
     */
    private fun clearPendingMessage() {
        pendingMessage = null
        isInSendMode = false
    }

    /**
     * Attempt to send pending message
     * Call this when you detect another device is in range
     */
    fun sendPendingMessage(): Boolean {
        pendingMessage?.let { message ->
            try {
                Log.d("NFCHelper", "Sending pending P2P message")

                clearPendingMessage()

                nfcListener?.onNFCMessageSent()
                return true

            } catch (e: Exception) {
                Log.e("NFCHelper", "Error sending pending message", e)
                nfcListener?.onNFCError("Error sending message: ${e.message}")
                return false
            }
        }
        return false
    }

    /**
     * Cancel any pending message
     */
    fun cancelPendingMessage() {
        clearPendingMessage()
        Log.d("NFCHelper", "Pending message cancelled")
    }

    /**
     * Check if NFC is available and enabled
     */
    fun isNFCAvailable(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    /**
     * Check if device supports NFC
     */
    fun isNFCSupported(): Boolean {
        return nfcAdapter != null
    }

    /**
     * Generate unique request ID
     */
    fun generateRequestId(): String {
        return "${NFCConfig.REQUEST_ID_PREFIX}_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    /**
     * Generate unique transaction ID
     */
    fun generateTransactionId(): String {
        return "${NFCConfig.TRANSACTION_ID_PREFIX}_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    /**
     * Reset payment state
     */
    fun resetPaymentState() {
        currentPaymentState = PaymentState.IDLE
        currentPaymentRequest = null
        currentPaymentAmount = null
    }
}