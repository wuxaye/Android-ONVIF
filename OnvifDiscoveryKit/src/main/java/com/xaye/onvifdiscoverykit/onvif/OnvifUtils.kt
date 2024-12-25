package com.xaye.onvifdiscoverykit.onvif

import android.content.Context
import com.xaye.onvifdiscoverykit.bean.OnvifDevice

/**
 * Author xaye
 * @date: 2024/12/24
 */
object OnvifUtils {
    /**
     * 通过用户名/密码/assets 文件获取对应需要发送的String
     * @param fileName assets 文件名
     * @param context context
     * @param device onvif 设备
     * @param needDigest 是否需要鉴权
     * @param params XML 参数
     * @return 需要发送的 String
     */
    fun getPostString(
        fileName: String,
        context: Context,
        device: OnvifDevice,
        needDigest: Boolean,
        vararg params: String
    ): String {
        // 读取文件内容
        val postString = context.assets.open(fileName).bufferedReader().use { it.readText() }

        return if (needDigest) {
            // 获取 digest
            val digest = Gsoap.getDigest(device.userName, device.psw)
            if (digest != null) {
                if (params.isNotEmpty()) {
                    val listParams = mutableListOf<String>().apply {
                        add(digest.userName)
                        add(digest.encodePsw)
                        add(digest.nonce)
                        add(digest.createdTime)
                        addAll(params)
                    }
                    String.format(postString, *listParams.toTypedArray())
                } else {
                    String.format(postString, digest.userName, digest.encodePsw, digest.nonce, digest.createdTime)
                }
            } else {
                postString // 如果没有 digest，直接返回原始的 postString
            }
        } else {
            // 如果不需要 digest，直接格式化 params
            String.format(postString, *params)
        }
    }
}