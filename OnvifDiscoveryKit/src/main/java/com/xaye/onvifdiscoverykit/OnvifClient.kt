package com.xaye.onvifdiscoverykit

import android.content.Context
import android.util.Log
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import com.xaye.onvifdiscoverykit.callback.DeviceDiscoveryCallback
import com.xaye.onvifdiscoverykit.utils.DeviceUtil
import com.xaye.onvifdiscoverykit.utils.FindDevices
import com.xaye.onvifdiscoverykit.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Author xaye
 * @date: 2024/12/23
 */
object OnvifClient {
    private var multicastAddress = "192.168.43.255" // 默认广播地址
    private val port = 3702 // 端口号
    private var timeout: Int = 5000 // 超时时间（毫秒）
    private var discoveryJob: Job? = null // 用于设备发现的协程任务
    private val isRunning = AtomicBoolean(false) // 标记当前是否正在进行发现任务

    //根据实际中的来填写，否则可能会验证失败！
    //海康需要打开onvif,并且添加用户，用户名和密码和你配置的一致即可
    private val credentials = mutableMapOf(
        "hikvision" to Pair("admin", "qwer123456"), // 海康默认账号密码
        "dahua" to Pair("admin", "admin")     // 大华默认账号密码
    )

    // 默认的账号密码，如果找不到厂商对应的凭证时使用
    private val defaultCredentials = Pair("admin", "123456")


    /**
     * 开始设备发现任务
     * @param context 应用上下文
     * @param callback 回调接口，用于处理设备发现的结果
     */
    fun startDiscovery(context: Context, callback: DeviceDiscoveryCallback) {
        if (isRunning.get()) {
            Logger.w("Discovery is already running.")
            return
        }

        multicastAddress = DeviceUtil.getBroadcastIp() ?: "239.255.255.250" // 获取广播地址，默认使用标准地址
        Logger.i("Start device discovery on: $multicastAddress")

        val findDevices = FindDevices(
            context = context,
            ipAddress = multicastAddress,
            port = port,
            timeout = timeout,
            callback = object : DeviceDiscoveryCallback {
                override fun onSearchStarted() {
                    isRunning.set(true)
                    callback.onSearchStarted()
                }

                override fun onDeviceFound(device: OnvifDevice) {
                    callback.onDeviceFound(device)
                }

                override fun onSearchFinished() {
                    isRunning.set(false)
                    callback.onSearchFinished()
                }

                override fun onSearchFailed(e: Throwable) {
                    isRunning.set(false)
                    callback.onSearchFailed(e)
                }
            }
        )

        discoveryJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                findDevices.startSearch() // 启动设备搜索
            } catch (e: Exception) {
                Logger.e("Error during discovery", e)
                isRunning.set(false)
                callback.onSearchFailed(e)
            }
        }
    }

    /**
     * 停止设备发现任务
     */
    fun stopDiscovery() {
        if (discoveryJob?.isActive == true) {
            discoveryJob?.cancel() // 取消协程任务
            isRunning.set(false) // 标记为已停止
            Logger.i("Device discovery stopped.")
        }
    }

    /**
     * 根据厂商名称获取对应的账号密码。
     * 如果厂商名称包含 credentials 中的关键字（不区分大小写），则返回对应的账号密码。
     * 否则，返回默认的账号密码。
     *
     * @param manufacturer 厂商名称
     * @return Pair<String, String> 账号和密码
     */
    fun getCredentials(manufacturer: String): Pair<String, String> {
        val lowerCaseManufacturer = manufacturer.lowercase()

        // 查找包含关键字的厂商，并返回对应的账号密码
        credentials.forEach { (key, credentialsPair) ->
            if (lowerCaseManufacturer.contains(key)) {
                return credentialsPair
            }
        }

        // 如果没有匹配的关键字，返回默认账号密码
        return defaultCredentials
    }

    /**
     * 自定义配置厂商账号密码
     * @param manufacturer 厂商名称
     * @param username 账号
     * @param password 密码
     */
    fun setCredentials(manufacturer: String, username: String, password: String) {
        credentials[manufacturer.lowercase()] = Pair(username, password)
    }

    /**
     * 设置广播地址
     * @param address 广播IP地址
     */
    fun setMulticastAddress(address: String) {
        multicastAddress = address
    }

    /**
     * 设置发现超时时间
     * @param timeout 超时时间（毫秒）
     */
    fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }
}