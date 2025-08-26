package com.dag.mypayandroid.base.helper.security

import android.content.Context
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class MyHostApduService : HostApduService() {

    companion object {
        private const val SELECT_APDU_HEADER = "00A40400"
        private const val AID = "F0010203040506" // Arbitrary ID
        private const val RESPONSE_OK = "9000"
        private const val RESPONSE_ERROR = "6F00"
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return hexStringToByteArray(RESPONSE_ERROR)

        val hexCommand = commandApdu.toHex()
        if (hexCommand.startsWith(SELECT_APDU_HEADER)) {
            // Get latest message from SharedPreferences
            val prefs = applicationContext.getSharedPreferences("nfc_prefs", Context.MODE_PRIVATE)
            val payload = prefs.getString("nfc_message", "No message")
            val payloadHex = payload!!.toByteArray(Charsets.UTF_8).toHex()
            return hexStringToByteArray(payloadHex + RESPONSE_OK)
        }
        return hexStringToByteArray(RESPONSE_ERROR)
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "Deactivated: $reason")
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }
    private fun String.toHex(): String = this.encodeToByteArray().joinToString("") { "%02X".format(it) }
    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }
}