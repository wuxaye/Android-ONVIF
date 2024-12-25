package com.xaye.onvifdiscoverykit.onvif

import android.util.Base64
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Author xaye
 * @date: 2024/12/24
 */
object Gsoap {

    /**
     * Digest = B64ENCODE( SHA1( B64DECODE( Nonce ) + Date + Password ) )
     * 生成 Digest
     */
    fun getDigest(userName: String, psw: String): Digest? {
        val nonce = getNonce()
        val time = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.getDefault()).format(Date())

        return try {
            val md = MessageDigest.getInstance("SHA-1")
            // Nonce需要用Base64解码一次
            val decodedNonce = Base64.decode(nonce.toByteArray(), Base64.DEFAULT)
            val dateBytes = time.toByteArray()
            val passwordBytes = psw.toByteArray()

            md.update(decodedNonce)
            md.update(dateBytes)
            md.update(passwordBytes)

            // 生成sha-1加密后的流
            val hashedBytes = md.digest()
            // 生成最终的加密字符串
            val result = String(Base64.encode(hashedBytes, Base64.DEFAULT)).trim { it <= ' ' }

            Digest(nonce, time, userName, result)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取 Nonce
     *
     * @return Nonce
     */
    private fun getNonce(): String {
        val text = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..32).map { text.random() }.joinToString("")
    }
}

/**
 * Data class to hold Digest details.
 */
data class Digest(
    val nonce: String,
    val createdTime: String,
    val userName: String,
    val encodePsw: String
)