package com.xaye.onvifdiscoverykit.callback

import com.xaye.onvifdiscoverykit.bean.OnvifDevice

/**
 * Author xaye
 * @date: 2024/12/24
 */
interface DeviceDiscoveryCallback {
    fun onSearchStarted() // 搜索开始
    fun onDeviceFound(device: OnvifDevice) // 发现设备
    fun onSearchFinished() // 搜索结束
    fun onSearchFailed(e: Throwable) // 搜索失败
}