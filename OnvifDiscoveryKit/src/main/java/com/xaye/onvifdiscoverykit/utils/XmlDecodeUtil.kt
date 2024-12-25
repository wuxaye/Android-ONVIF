package com.xaye.onvifdiscoverykit.utils

import android.util.Log
import android.util.Xml
import com.xaye.onvifdiscoverykit.bean.ImageSetting
import com.xaye.onvifdiscoverykit.bean.MediaProfile
import com.xaye.onvifdiscoverykit.bean.NetworkInterface
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Author xaye
 * @date: 2024/12/23
 */
object XmlDecodeUtil {
    /**
     * 获取设备信息
     */
    @Throws(Exception::class)
    fun getDeviceInfo(xml: String): OnvifDevice {
        val device = OnvifDevice()
        val parser = Xml.newPullParser()
        val input = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "XAddrs" -> { // 解析设备服务地址
                            val addrs = parser.nextText()
                            val url = addrs.split(" ")[0]
                            device.serviceUrl = url
                        }
                        "MessageID" -> { // 解析设备 UUID
                            device.uuid = parser.nextText()
                        }
                        "Scopes" -> { // 解析厂商信息
                            val manufacturerInfo = extractManufacturerFromScopes(parser.nextText())
                            manufacturerInfo?.let { device.manufacturer = it }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return device
    }

    /**
     * 从 <Scopes> 字符串中提取厂商信息
     */
    private fun extractManufacturerFromScopes(scopes: String?): String? {
        scopes ?: return null
        val prefix = "onvif://www.onvif.org/name/"
        val startIndex = scopes.indexOf(prefix)
        if (startIndex != -1) {
            val endIndex = scopes.indexOf(" ", startIndex).takeIf { it != -1 } ?: scopes.length
            return scopes.substring(startIndex + prefix.length, endIndex).replace("%20", " ")
        }
        return null
    }

    /**
     * 解析 xml数据，获取 MediaUrl, PtzUrl
     */
    @Throws(Exception::class)
    fun getCapabilitiesUrl(xml: String, device: OnvifDevice) {
        val parser = Xml.newPullParser()
        val input = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Media" -> { // 解析媒体服务地址
                            parser.nextTag()
                            if (parser.name == "XAddr") device.mediaUrl = parser.nextText()
                        }
                        "PTZ" -> { // 解析云台服务地址
                            parser.nextTag()
                            if (parser.name == "XAddr") device.ptzUrl = parser.nextText()
                        }
                        "Events" -> { // 解析事件服务地址
                            parser.nextTag()
                            if (parser.name == "XAddr") device.eventUrl = parser.nextText()
                        }
                        "Analytics" -> { // 解析分析服务地址
                            parser.nextTag()
                            if (parser.name == "XAddr") device.analyticsUrl = parser.nextText()
                        }
                        "Imaging" -> { // 解析图像服务地址
                            parser.nextTag()
                            if (parser.name == "XAddr") device.imageUrl = parser.nextText()
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }

    /**
     * 解析 xml数据，获取设备信息
     */
    @Throws(Exception::class)
    fun getDeviceInformation(xml: String, device: OnvifDevice) {
        val parser = Xml.newPullParser()
        val input = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "FirmwareVersion" -> device.firmwareVersion = parser.nextText() // 解析固件版本
                        "SerialNumber" -> device.serialNumber = parser.nextText() // 解析设备序列号
                        "Manufacturer" -> device.manufacturer = parser.nextText() // 解析厂商信息
                        "Model" -> device.model = parser.nextText() // 解析设备型号
                    }
                }
            }
            eventType = parser.next()
        }
    }

    /**
     * 解析 xml数据，获取图像设置
     */
    @Throws(Exception::class)
    fun getImageSetting(xml: String, device: OnvifDevice) {
        val parser = Xml.newPullParser()
        val input = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")

        var eventType = parser.eventType
        var tag = ""
        val imageSetting = ImageSetting()
        var exposure: ImageSetting.Exposure? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Brightness" -> imageSetting.brightness = parser.nextText().toFloat() // 解析亮度
                        "ColorSaturation" -> imageSetting.colorSaturation = parser.nextText().toFloat() // 解析色彩饱和度
                        "Contrast" -> imageSetting.contrast = parser.nextText().toFloat() // 解析对比度
                        "Exposure" -> { // 解析曝光设置
                            exposure = ImageSetting.Exposure()
                            imageSetting.exposure = exposure
                            tag = "Exposure"
                        }
                        "Mode" -> if (tag == "Exposure") exposure?.mode = parser.nextText() // 解析曝光模式
                        "MinExposureTime" -> if (tag == "Exposure") exposure?.minExposureTime = parser.nextText().toInt() // 解析最小曝光时间
                        "MaxExposureTime" -> if (tag == "Exposure") exposure?.maxExposureTime = parser.nextText().toInt() // 解析最大曝光时间
                        "ExposureTime" -> if (tag == "Exposure") exposure?.exposureTime = parser.nextText().toFloat() // 解析曝光时间
                    }
                }
            }
            eventType = parser.next()
        }
        device.imageSetting = imageSetting // 设置图像设置
    }

    /**
     * 解析 xml 数据，获取 MediaUrl 和 PtzUrl
     *
     * @param xml    需要解析的 XML 数据
     * @param device 对应的 Device 对象
     */
    @Throws(Exception::class)
    fun getNetworkInterface(xml: String, device: OnvifDevice) {
        var networkInterface = device.networkInterface
        if (networkInterface == null) {
            networkInterface = NetworkInterface()
            device.networkInterface = networkInterface
        }

        val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
        //启用命名空间解析： 标签名为 Profiles。
        //禁用命名空间解析： 标签名为 tt:Profiles。
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        val input: InputStream = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")
        var eventType = parser.eventType

        var tag = ""
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> { }
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "NetworkInterfaces" -> {
                            // 获取 token
                            val interfaceToken = parser.getAttributeValue(null, "token")
                            networkInterface.interfaceToken = interfaceToken
                        }
                        "MTU" -> {
                            // 获取 MTU
                            val mtu = parser.nextText()
                            networkInterface.mtu = mtu
                        }
                        "IPv4" -> {
                            // IPv4 开始标签
                            tag = "IPv4"
                        }
                        "PrefixLength" -> {
                            if (tag == "IPv4") {
                                // 获取 PrefixLength
                                val ipvtPrefixLength = parser.nextText()
                                networkInterface.ipvtPrefixLength = ipvtPrefixLength
                            }
                        }
                        "IPv6" -> {
                            // IPv6 开始标签
                            tag = "IPv6"
                        }
                    }
                }
                XmlPullParser.END_TAG -> { }
                else -> { }
            }
            eventType = parser.next()
        }
    }

    /**
     * 解析 xml 数据，获取 MediaProfile
     *
     * @param xml 需要解析的 XML 数据
     * @return 返回解析后的 MediaProfile 列表
     */
    @Throws(Exception::class)
    fun getMediaProfiles(xml: String): ArrayList<MediaProfile> {
        val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
        //启用命名空间解析： 标签名为 Profiles。
        //禁用命名空间解析： 标签名为 tt:Profiles。
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)

        val profiles = ArrayList<MediaProfile>()
        var profile: MediaProfile? = null
        var tag = ""
        val input: InputStream = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> { }
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Profiles" -> {
                            // 获取 Profiles 节点
                            profile = MediaProfile()
                            val token = parser.getAttributeValue(null, "token")
                            profile.token = token
                            parser.next()
                            if (parser.name != null && parser.name == "Name") {
                                profile.name = parser.nextText()
                            }
                        }
                        "VideoEncoderConfiguration" -> {
                            // 获取 VideoEncoder 配置
                            val token = parser.getAttributeValue(null, "token")
                            profile?.videoEncode?.token = token
                            tag = "Video"
                        }
                        "AudioEncoderConfiguration" -> {
                            // 获取 AudioEncoder 配置
                            val tokenName = parser.getAttributeValue(null, "token")
                            profile?.audioEncode?.tokenName = tokenName
                            tag = "Audio"
                        }
                        "VideoSourceConfiguration" -> {
                            // 获取 VideoSource 配置
                            val videoSourceConfigurationToken = parser.getAttributeValue(null, "token")
                            profile?.videoSource?.videoSourceConfigurationToken = videoSourceConfigurationToken
                            tag = "videoSource"
                        }
                        "AudioSourceConfiguration" -> {
                            // 获取 AudioSource 配置
                            val audioSourceConfigurationToken = parser.getAttributeValue(null, "token")
                            profile?.audioSource?.audioSourceConfigurationToken = audioSourceConfigurationToken
                            tag = "audioSource"
                        }
                        "Name" -> {
                            // 获取 Source 的名称
                            if (tag == "videoSource") {
                                val text = parser.nextText()
                                profile?.videoSource?.name = text
                            }
                        }
                        "UseCount" -> {
                            // 获取 UseCount
                            if (tag == "videoSource") {
                                val text = parser.nextText()
                                profile?.videoSource?.userCount = text
                            }
                        }
                        "SourceToken" -> {
                            // 获取 SourceToken
                            if (tag == "videoSource") {
                                val videoSourceToken = parser.nextText()
                                profile?.videoSource?.videoSourceToken = videoSourceToken
                            } else if (tag == "audioSource") {
                                val audioSourceToken = parser.nextText()
                                profile?.audioSource?.audioSourceToken = audioSourceToken
                            }
                        }
                        "Bounds" -> {
                            // 获取 Bounds
                            if (tag == "videoSource") {
                                val height = parser.getAttributeValue(null, "height")
                                val width = parser.getAttributeValue(null, "width")
                                profile?.videoSource?.height = height?.toInt() ?: 0
                                profile?.videoSource?.width = width?.toInt() ?: 0
                            }
                        }
                        "Width" -> {
                            // 获取视频分辨率宽度
                            val text = parser.nextText()
                            if (tag == "Video") {
                                profile?.videoEncode?.width = text.toInt()
                            }
                        }
                        "Height" -> {
                            // 获取视频分辨率高度
                            val text = parser.nextText()
                            if (tag == "Video") {
                                profile?.videoEncode?.height = text.toInt()
                            }
                        }
                        "FrameRateLimit" -> {
                            // 获取帧率
                            val text = parser.nextText()
                            if (tag == "Video") {
                                profile?.videoEncode?.frameRate = text.toInt()
                            }
                        }
                        "Encoding" -> {
                            // 获取编码格式
                            val text = parser.nextText()
                            if (tag == "Video") {
                                profile?.videoEncode?.encoding = text
                            } else if (tag == "Audio") {
                                profile?.audioEncode?.encoding = text
                            }
                        }
                        "Bitrate" -> {
                            // 获取 Bitrate
                            val text = parser.nextText()
                            if (tag == "Audio") {
                                profile?.audioEncode?.bitrate = text.toInt()
                            }
                        }
                        "SampleRate" -> {
                            // 获取 SampleRate
                            val text = parser.nextText()
                            if (tag == "Audio") {
                                profile?.audioEncode?.sampleRate = text.toInt()
                            }
                        }
                        "PTZConfiguration" -> {
                            // 获取 PTZ 配置
                            profile?.ptzConfiguration?.token = parser.getAttributeValue(0)
                            tag = "Ptz"
                        }
                        "NodeToken" -> {
                            // 获取 NodeToken
                            if (tag == "Ptz") {
                                val text = parser.nextText()
                                profile?.ptzConfiguration?.nodeToken = text
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "Profiles") {
                        profile?.let { profiles.add(it) }
                    }
                    if (listOf("AudioEncoderConfiguration", "VideoEncoderConfiguration", "PTZConfiguration").contains(parser.name)) {
                        tag = ""
                    }
                }
                else -> { }
            }
            eventType = parser.next()
        }
        return profiles
    }

    /**
     * 获取 RTSP URL
     */
    @Throws(Exception::class)
    fun getStreamUri(xml: String): String {
        var mediaUrl = ""
        val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
        //启用命名空间解析： 标签名为 Profiles。
        //禁用命名空间解析： 标签名为 tt:Profiles。
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        val input: InputStream = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> { }
                XmlPullParser.START_TAG -> {
                    if (parser.name == "Uri") {
                        mediaUrl = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> { }
                else -> { }
            }
            eventType = parser.next()
        }
        return mediaUrl
    }

    /**
     * 获取截图 URI
     */
    @Throws(Exception::class)
    fun getSnapshotUri(xml: String): String {
        return getUriByTag(xml, "Uri")
    }

    /**
     * 获取上传 URI
     */
    @Throws(Exception::class)
    fun getUploadUri(xml: String): String {
        return getUriByTag(xml, "UploadUri")
    }

    /**
     * 通用方法获取 Uri
     */
    @Throws(Exception::class)
    private fun getUriByTag(xml: String, tag: String): String {
        var uri = ""
        val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
        //启用命名空间解析： 标签名为 Profiles。
        //禁用命名空间解析： 标签名为 tt:Profiles。
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        val input: InputStream = ByteArrayInputStream(xml.toByteArray())
        parser.setInput(input, "UTF-8")
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> { }
                XmlPullParser.START_TAG -> {
                    if (parser.name == tag) {
                        uri = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> { }
                else -> { }
            }
            eventType = parser.next()
        }
        return uri
    }
}