package com.xaye.onvifdiscoverykit.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Author xaye
 * @date: 2024/12/23
 */
@Parcelize
data class ImageSetting(
    var brightness: Float = 0f, // 亮度
    var colorSaturation: Float = 0f, // 色彩饱和度
    var contrast: Float = 0f, // 对比度
    var exposure: Exposure = Exposure() // 曝光设置
) : Parcelable {
    @Parcelize
    data class Exposure(
        var mode: String? = null, // 曝光模式
        var minExposureTime: Int = 0, // 最小曝光时间
        var maxExposureTime: Int = 0, // 最大曝光时间
        var exposureTime: Float = 0f // 曝光时间
    ) : Parcelable {
        override fun toString(): String {
            return "Exposure(mode=$mode, minExposureTime=$minExposureTime, maxExposureTime=$maxExposureTime, exposureTime=$exposureTime)"
        }
    }

    override fun toString(): String {
        return "ImageSetting(brightness=$brightness, colorSaturation=$colorSaturation, contrast=$contrast, exposure=$exposure)"
    }
}
