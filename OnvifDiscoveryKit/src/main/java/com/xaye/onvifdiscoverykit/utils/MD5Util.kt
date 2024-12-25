package com.xaye.onvifdiscoverykit.utils

import java.security.MessageDigest

/**
 * Author xaye
 * @date: 2024/12/24
 */
object MD5Util {
    private val hexDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")

    /**
     * 把字节数组转换为十六进制的字符串
     * @param bytes 字节数组
     * @return 十六进制的字符串
     */
    private fun byteArrayToHexString(bytes: ByteArray): String {
        val resultSb = StringBuilder()
        for (byte in bytes) {
            resultSb.append(byteToHexString(byte))
        }
        return resultSb.toString()
    }

    /**
     * 把一个字节转换为一个十六进制的字符串
     * @param byte 字节
     * @return 十六进制的字符串
     */
    private fun byteToHexString(byte: Byte): String {
        var n = byte.toInt()
        if (n < 0) {
            n += 256
        }
        val d1 = n / 16
        val d2 = n % 16
        return hexDigits[d1] + hexDigits[d2]
    }

    /**
     * 把字符串以MD5的方式加密
     * @param origin 需要加密的字符串
     * @return 加密后的字符串
     */
    fun MD5Encode(origin: String): String {
        var resultString = origin
        try {
            val md = MessageDigest.getInstance("MD5")
            resultString = byteArrayToHexString(md.digest(resultString.toByteArray()))
        } catch (e: Exception) {
            e.printStackTrace() // 错误处理
        }
        return resultString
    }
}