package com.xaye.onvifdiscoverykit.utils

import android.content.Context
import com.xaye.onvifdiscoverykit.OnvifClient
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import com.xaye.onvifdiscoverykit.callback.DeviceDiscoveryCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.UUID

/**
 * Author xaye
 * @date: 2024/12/23
 */
class FindDevices(
    private val context: Context, // 应用程序上下文
    private val ipAddress: String, // 要广播的目标IP地址
    private val port: Int, // 广播目标端口
    private val timeout: Int, // 搜索超时时间
    private val callback: DeviceDiscoveryCallback // 设备发现结果的回调接口
) {
    companion object {
        private const val PROBE_FILE = "probe.xml" // 探测文件
        private const val BUFFER_SIZE = 4 * 1024 // 接收缓冲区大小
    }

    private lateinit var sendData: ByteArray // 发送的探测数据

    init {
        initializeSendData() // 初始化发送数据
    }

    // 初始化发送数据
    private fun initializeSendData() {
        try {
            // 打开assets目录下的probe.xml文件
            context.assets.open(PROBE_FILE).use { inputStream ->
                val uuid = UUID.randomUUID().toString() // 生成唯一的UUID
                val probeTemplate = String(inputStream.readBytes()) // 读取文件内容为字符串
                sendData = String.format(probeTemplate, uuid).toByteArray() // 将UUID插入到模板中
            }
        } catch (e: Exception) {
            // 捕获异常并通过回调通知错误
            Logger.e("Failed to read $PROBE_FILE", e)
            callback.onSearchFailed(e)
        }
    }

    // 开始设备搜索
    fun startSearch() {
        if (!::sendData.isInitialized) {
            // 如果发送数据未初始化，抛出异常
            callback.onSearchFailed(Exception("无法读取 $PROBE_FILE"))
            return
        }

        callback.onSearchStarted() // 通知回调，搜索开始

        CoroutineScope(Dispatchers.IO).launch {
            try {
                searchDevices() // 执行搜索设备逻辑
            } catch (e: Exception) {
                Logger.e("Search failed", e)
                callback.onSearchFailed(e) // 搜索失败回调
            } finally {
                withContext(Dispatchers.Main) {
                    callback.onSearchFinished() // 搜索结束回调
                }
            }
        }
    }

    // 搜索设备的核心逻辑
    private suspend fun searchDevices() {
        withContext(Dispatchers.IO) {
            DatagramSocket().use { udpSocket ->
                udpSocket.apply {
                    soTimeout = timeout // 设置套接字超时时间
                    broadcast = true // 设置为广播模式
                }

                try {
                    // 创建发送数据包
                    val sendPacket = DatagramPacket(
                        sendData,
                        sendData.size,
                        InetAddress.getByName(ipAddress), // 广播目标IP
                        port // 广播目标端口
                    )
                    udpSocket.send(sendPacket) // 发送广播数据包
                    Logger.d("Sent probe packet to $ipAddress:$port")

                    val buffer = ByteArray(BUFFER_SIZE) // 创建接收缓冲区
                    val receivePacket = DatagramPacket(buffer, buffer.size) // 创建接收数据包
                    val endTime = System.currentTimeMillis() + timeout // 计算搜索结束时间

                    while (System.currentTimeMillis() < endTime) {
                        try {
                            udpSocket.receive(receivePacket) // 接收响应数据包
                            val response =
                                String(receivePacket.data, 0, receivePacket.length) // 将数据转换为字符串
                            Logger.d("Received response: $response")

                            // 解析响应数据，提取设备信息
                            XmlDecodeUtil.getDeviceInfo(response)?.let { device ->
                                if (device.serviceUrl != null && device.ipAddress != null) {
                                    applyCredentials(device) // 应用设备凭据
                                    fetchDeviceInfo(device) // 获取设备详细信息
                                }
                            }
                        } catch (e: SocketTimeoutException) {
                            // 超时结束搜索
                            Logger.d("Socket timeout, continuing to listen for responses")
                        } catch (e: Exception) {
                            // 捕获其他接收数据异常
                            Logger.e("Error receiving packet", e)
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("Error during device search", e)
                    throw e // 抛出异常以供外层捕获
                }
            }
        }
    }

    // 应用设备凭据（用户名和密码）
    private fun applyCredentials(device: OnvifDevice) {
        val manufacturer = device.manufacturer?.lowercase() ?: "" // 获取设备制造商名称（小写）
        OnvifClient.getCredentials(manufacturer).let { (username, password) ->
            device.userName = username // 设置设备用户名
            device.psw = password // 设置设备密码
            Logger.i("Applied credentials for $manufacturer: $username / $password")
        }
    }

    // 异步获取设备详细信息
    private suspend fun fetchDeviceInfo(device: OnvifDevice) {
        GetDeviceInfo(device, context) { result ->
            result.onSuccess { deviceInfo ->
                // 设备信息获取成功
                Logger.i("设备信息获取成功: ${deviceInfo.ipAddress}")
                callback.onDeviceFound(deviceInfo) // 通知回调，发现设备
            }.onFailure { exception ->
                // 设备信息获取失败
                Logger.e("设备信息获取失败: ${exception.message}")
                callback.onSearchFailed(exception) // 通知回调，获取失败
            }
        }.run()
    }
}
