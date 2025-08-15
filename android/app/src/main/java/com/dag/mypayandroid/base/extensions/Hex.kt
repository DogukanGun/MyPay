package com.dag.mypayandroid.base.extensions

fun String.hexToByteArray(): ByteArray {
    val cleanInput = this.removePrefix("0x").lowercase()
    require(cleanInput.length % 2 == 0) { "Hex string must have even length" }
    val byteArray = ByteArray(cleanInput.length / 2)
    var i = 0
    while (i < cleanInput.length) {
        val byte = cleanInput.substring(i, i + 2).toInt(16)
        byteArray[i / 2] = byte.toByte()
        i += 2
    }
    return byteArray
}

fun ByteArray.toHexString(): String {
    return joinToString("") { String.format("%02x", it) }
} 