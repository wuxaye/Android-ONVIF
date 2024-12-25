package com.xaye.onvifdiscoverykit.utils

import android.content.Context
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import com.xaye.onvifdiscoverykit.onvif.OnvifUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Author xaye
 * @date: 2024/12/24
 */
class GetDeviceInfo(
    private val device: OnvifDevice,
    private val context: Context,
    private val callBack: (Result<OnvifDevice>) -> Unit
) {

    companion object {
        private const val TAG = "GetDeviceInfo"
    }

    /**
     * 执行获取设备信息的操作。
     */
    suspend fun run() {
        try {
            // 获取设备能力集合 (不需要鉴权)
            fetchAndParse(
                xmlFile = "getCapabilities.xml",
                needDigest = false
            ) { response ->
                XmlDecodeUtil.getCapabilitiesUrl(response, device)
            }

            // 获取设备信息 (需要鉴权)
            fetchAndParse(
                xmlFile = "getDeviceInformation.xml",
                needDigest = true
            ) { response ->
                XmlDecodeUtil.getDeviceInformation(response, device)
            }

            // 获取网络接口配置 (需要鉴权)
            fetchAndParse(
                xmlFile = "getNetworkInterface.xml",
                needDigest = true
            ) { response ->
                XmlDecodeUtil.getNetworkInterface(response, device)
            }

            // 获取媒体配置文件 (需要鉴权，使用 mediaUrl)
            fetchAndParse(
                xmlFile = "getProfiles.xml",
                needDigest = true,
                urlOverride = device.mediaUrl
            ) { response ->
                device.addProfiles(XmlDecodeUtil.getMediaProfiles(response))
            }

            // 获取每个配置文件的 RTSP URL (需要鉴权，使用 mediaUrl)
            device.profiles.forEach { profile ->
                fetchAndParse(
                    xmlFile = "getStreamUri.xml",
                    needDigest = true,
                    urlOverride = device.mediaUrl,
                    params = profile.token ?: ""
                ) { response ->
                    profile.rtspUrl = XmlDecodeUtil.getStreamUri(response)
                }
            }

            // 使用 Result 成功回调
            withContext(Dispatchers.Main) {
                callBack(Result.success(device))
            }

        } catch (e: Exception) {
            Logger.e("Failed to fetch device info for ${device.ipAddress}", e)
            withContext(Dispatchers.Main) {
                callBack(Result.failure(e))
            }
        }
    }

    /**
     * 发送 HTTP 请求并解析响应的通用函数。
     *
     * @param xmlFile 要读取的 XML 文件名。
     * @param needDigest 是否需要鉴权。
     * @param urlOverride 可选的 URL 覆盖参数。
     * @param params 可选的 XML 参数，用于格式化请求字符串。
     * @param parser 解析响应的高阶函数。
     */
    private suspend fun fetchAndParse(
        xmlFile: String,
        needDigest: Boolean,
        urlOverride: String? = null,
        params: String = "",
        parser: (String) -> Unit
    ) {
        try {
            // 获取要发送的 POST 字符串
            val postString = OnvifUtils.getPostString(xmlFile, context, device, needDigest, params)
            // 确定请求的 URL
            val url = urlOverride ?: device.serviceUrl
            // 发送 HTTP POST 请求并获取响应
            val response = withContext(Dispatchers.IO) { HttpUtil.postRequest(url, postString) }
            // 解析响应并更新设备信息
            parser(response)
        } catch (e: Exception) {
            Logger.e("Error fetching or parsing $xmlFile for device ${device.ipAddress}", e)
            throw e
        }
    }
}