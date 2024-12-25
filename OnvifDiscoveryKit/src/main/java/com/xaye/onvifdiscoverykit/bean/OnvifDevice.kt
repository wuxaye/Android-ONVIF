package com.xaye.onvifdiscoverykit.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Author xaye
 * @date: 2024/12/23
 */
@Parcelize
data class OnvifDevice(
    var userName: String = "", // 默认用户名
    var psw: String = "", // 默认密码

    // IP 地址
    var ipAddress: String? = null,

    var uuid: String? = null,

    // 通过 getDeviceInformation 获得的设备信息
    var firmwareVersion: String? = null, // 固件版本
    var manufacturer: String? = null, // 厂商信息
    var serialNumber: String? = null, // 序列号
    var model: String? = null, // 设备型号

    // 通过 getCapabilities 获得的设备能力
    var mediaUrl: String? = null, // 媒体 URL
    var ptzUrl: String? = null, // PTZ 控制 URL
    var imageUrl: String? = null, // 图像 URL
    var eventUrl: String? = null, // 事件 URL
    var analyticsUrl: String? = null, // 分析 URL

    // ONVIF 媒体配置文件
    var profiles: ArrayList<MediaProfile> = ArrayList(), // 存储 ONVIF 媒体配置文件列表

    // 网络接口和图像设置
    var networkInterface: NetworkInterface? = null, // 网络接口
    var imageSetting: ImageSetting? = null // 图像设置
) : Parcelable {

    // 通过发现获得的服务 URL
    var serviceUrl: String? = null
        set(value) {
            field = value
            ipAddress = value?.substringAfter("//")?.substringBefore("/on")
        }

    fun addProfiles(profiles: ArrayList<MediaProfile>) {
        this.profiles.clear()
        this.profiles.addAll(profiles)
    }
    // 自定义 toString 方法
    override fun toString(): String {
        return "OnvifDevice(userName='$userName', psw='$psw', ipAddress='$ipAddress', serviceUrl=$serviceUrl, uuid='$uuid', firmwareVersion='$firmwareVersion', manufacturer='$manufacturer', serialNumber='$serialNumber', model='$model', mediaUrl='$mediaUrl', ptzUrl='$ptzUrl', imageUrl='$imageUrl', eventUrl='$eventUrl', analyticsUrl='$analyticsUrl', profiles=$profiles, networkInterface=$networkInterface, imageSetting=$imageSetting)"
    }
}