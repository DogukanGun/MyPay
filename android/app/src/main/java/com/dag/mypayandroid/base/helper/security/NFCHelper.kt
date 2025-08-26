package com.dag.mypayandroid.base.helper.security

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.util.Log
import java.nio.charset.StandardCharsets

class NFCHelper(private val activity: Activity?) {

    val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    val pendingIntent: PendingIntent

    // This will hold the message we want to send.
    private var messageToSend: NdefMessage? = null

    interface NFCListener {
        fun onMessageReceived(message: String)
        fun onNFCError(error: String)
    }

    private var listener: NFCListener? = null

    init {
        // Create a PendingIntent to handle NFC intents when the app is in the foreground.
        val intent = Intent(activity, activity?.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        // Use FLAG_IMMUTABLE for security with modern Android versions.
        pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    fun setListener(listener: NFCListener) {
        this.listener = listener
    }

    /**
     * Prepares a message to be sent via NFC.
     * This message is stored and waits for the Android system to request it.
     */
    fun sendMessage(data: String) {
        if (nfcAdapter?.isEnabled != true) {
            listener?.onNFCError("NFC is not enabled.")
            return
        }
        // Create an NDEF message with a custom MIME type record.
        val mimeRecord = NdefRecord.createMime(
            NFCConfig.CUSTOM_MIME_TYPE, // Using configured MIME type
            data.toByteArray(StandardCharsets.UTF_8)
        )
        messageToSend = NdefMessage(mimeRecord)
        Log.d("NFCHelper", "Message is ready to be sent.")
    }

    /**
     * This function is called by the Android system (via the Activity callback)
     * when another NFC device is in range. It provides the message and then
     * clears it, ensuring it's only sent once per `sendMessage` call.
     */
    fun getAndClearMessageToSend(): NdefMessage? {
        val message = messageToSend
        messageToSend = null // Clear the message after providing it.
        return message
    }

    /**
     * Processes an incoming NFC intent to extract the message.
     */
    fun handleIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            try {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                rawMessages?.firstOrNull()?.let {
                    val message = it as NdefMessage
                    val record = message.records.firstOrNull()
                    // Extract the payload (the actual data) from the first record.
                    val payload = record?.payload?.toString(StandardCharsets.UTF_8)
                    if (payload != null) {
                        listener?.onMessageReceived(payload)
                    } else {
                        listener?.onNFCError("Received an empty NFC message.")
                    }
                }
            } catch (e: Exception) {
                Log.e("NFCHelper", "Error processing NDEF message", e)
                listener?.onNFCError("Error processing received message.")
            }
        }
    }
}