package com.dag.mypayandroid.base.helper.security

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log

enum class NFCMode { TAG, READER }

class NFCHelper(private val activity: Activity?) {

    val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var listener: NFCListener? = null
    private var messageToSend: String? = null
    private var currentMode: NFCMode = NFCMode.READER

    companion object {
        private const val TAG = "NFCHelper"
        private const val SELECT_APDU = "00A40400"
        private const val AID = "F0010203040506"
    }

    interface NFCListener {
        fun onMessageReceived(message: String)
        fun onNFCError(error: String)
        fun onNFCStateChanged(mode: NFCMode)
    }

    fun setListener(listener: NFCListener) {
        this.listener = listener
    }

    fun setMode(mode: NFCMode) {
        Log.d(TAG, "Switching from $currentMode to $mode")
        currentMode = mode
        listener?.onNFCStateChanged(mode)

        when (mode) {
            NFCMode.READER -> {
                Log.d(TAG, "Enabling READER mode - ready to receive messages")
                enableReaderMode()
                // Clear any stored message when switching to reader mode
                activity?.getSharedPreferences("nfc_prefs", Context.MODE_PRIVATE)
                    ?.edit()?.remove("nfc_message")?.apply()
            }

            NFCMode.TAG -> {
                Log.d(TAG, "Enabling TAG mode (HCE) - ready to send messages")
                disableReaderMode()
                // HCE service will automatically handle incoming connections
            }
        }
    }

    fun sendMessage(data: String) {
        if (currentMode != NFCMode.TAG) {
            listener?.onNFCError("Switch to TAG mode to send messages.")
            return
        }

        messageToSend = data
        // Save into shared preference so ApduService can read it
        val prefs = activity?.getSharedPreferences("nfc_prefs", Context.MODE_PRIVATE)
        val success = prefs?.edit()?.putString("nfc_message", data)?.commit() ?: false

        if (success) {
            Log.d(TAG, "Message stored for HCE: $data")
            listener?.onNFCStateChanged(NFCMode.TAG) // Notify that we're ready to send
        } else {
            listener?.onNFCError("Failed to store message for transmission")
        }
    }

    private fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(
            activity,
            { tag -> handleTagDiscovered(tag) },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
        Log.d(TAG, "Reader mode enabled")
    }

    private fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(activity)
        Log.d(TAG, "Reader mode disabled")
    }

    private fun handleTagDiscovered(tag: Tag) {
        if (currentMode != NFCMode.READER) return

        Log.d(TAG, "Tag discovered: ${tag.id.joinToString { "%02X".format(it) }}")

        try {
            val isoDep = IsoDep.get(tag)
            if (isoDep != null) {
                isoDep.connect()

                // Send SELECT command to the HCE service
                val selectCommand = buildSelectCommand()
                Log.d(TAG, "Sending SELECT command: ${selectCommand.joinToString { "%02X".format(it) }}")

                val response = isoDep.transceive(selectCommand)
                Log.d(TAG, "Received response: ${response.joinToString { "%02X".format(it) }}")

                // Parse response
                if (response.size >= 2) {
                    val statusCode = response.sliceArray(response.size - 2 until response.size)
                    if (statusCode.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                        // Success - extract payload
                        val payload = response.sliceArray(0 until response.size - 2)
                        val message = String(payload, Charsets.UTF_8)
                        Log.d(TAG, "Successfully received message: $message")

                        activity?.runOnUiThread {
                            listener?.onMessageReceived(message)
                        }
                    } else {
                        activity?.runOnUiThread {
                            listener?.onNFCError("HCE service returned error")
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        listener?.onNFCError("Invalid response from HCE service")
                    }
                }

                isoDep.close()
            } else {
                activity?.runOnUiThread {
                    listener?.onNFCError("Tag doesn't support ISO-DEP")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error communicating with tag", e)
            activity?.runOnUiThread {
                listener?.onNFCError("Error reading from device: ${e.message}")
            }
        }
    }

    private fun buildSelectCommand(): ByteArray {
        val aid = hexStringToByteArray(AID)
        val command = ByteArray(5 + aid.size)
        command[0] = 0x00.toByte() // CLA
        command[1] = 0xA4.toByte() // INS (SELECT)
        command[2] = 0x04.toByte() // P1
        command[3] = 0x00.toByte() // P2
        command[4] = aid.size.toByte() // LC
        System.arraycopy(aid, 0, command, 5, aid.size)
        return command
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

    fun getCurrentMode(): NFCMode = currentMode

    fun isNfcEnabled(): Boolean = nfcAdapter?.isEnabled == true
}