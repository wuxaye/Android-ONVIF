package com.xaye.onvifdiscoverykit.utils

import java.net.NetworkInterface
import java.net.SocketException

/**
 * Author xaye
 * @date: 2024/12/24
 */
object DeviceUtil {

    fun getBroadcastIp(): String? {
        try {
            // 优先使用 IPv4
            System.setProperty("java.net.preferIPv4Stack", "true")

            // 遍历所有网络接口
            NetworkInterface.getNetworkInterfaces().iterator().forEach { networkInterface ->
                // 跳过回环接口
                if (!networkInterface.isLoopback && networkInterface.isUp) {
                    // 遍历接口地址
                    networkInterface.interfaceAddresses.forEach { interfaceAddress ->
                        // 返回第一个非空的广播地址
                        interfaceAddress.broadcast?.let { return it.hostAddress }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return null
    }

}