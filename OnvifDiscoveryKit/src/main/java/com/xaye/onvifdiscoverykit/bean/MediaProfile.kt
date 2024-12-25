package com.xaye.onvifdiscoverykit.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Author xaye
 * @date: 2024/12/23
 */
@Parcelize
data class MediaProfile(
    // Token
    var token: String? = null,
    // 名称
    var name: String? = null,
    // 视频编码配置
    var videoEncode: VideoEncoderConfiguration = VideoEncoderConfiguration(),
    // 视频源配置
    var videoSource: VideoSourceConfiguration = VideoSourceConfiguration(),
    // 音频编码配置
    var audioEncode: AudioEncoderConfiguration = AudioEncoderConfiguration(),
    // 音频源配置
    var audioSource: AudioSourceConfiguration = AudioSourceConfiguration(),
    // PTZ配置
    var ptzConfiguration: PTZConfiguration = PTZConfiguration(),
    // RTSP URL
    var rtspUrl: String? = null
) : Parcelable

@Parcelize
data class VideoEncoderConfiguration(
    var token: String? = null,
    var encoding: String? = null, // 编码格式
    var width: Int = 0, // 分辨率宽度
    var height: Int = 0, // 分辨率高度
    var frameRate: Int = 0 // 帧率
) : Parcelable

@Parcelize
data class VideoSourceConfiguration(
    var videoSourceConfigurationToken: String? = null,
    var name: String? = null,
    var userCount: String? = null,
    var videoSourceToken: String? = null,
    var width: Int = 0, // 分辨率宽度
    var height: Int = 0 // 分辨率高度
): Parcelable

@Parcelize
data class AudioEncoderConfiguration(
    var tokenName: String? = null,
    var encoding: String? = null, // 编码格式
    var audioSourceToken: String? = null,
    var sampleRate: Int = 0, // 采样率
    var bitrate: Int = 0 // 比特率
): Parcelable

@Parcelize
data class AudioSourceConfiguration(
    var audioSourceConfigurationToken: String? = null,
    var name: String? = null,
    var userCount: String? = null,
    var audioSourceToken: String? = null,
    var width: Int = 0, // 分辨率宽度（如适用）
    var height: Int = 0 // 分辨率高度（如适用）
): Parcelable

@Parcelize
data class PTZConfiguration(
    var token: String? = null, // PTZ token
    var nodeToken: String? = null // 节点 token
): Parcelable


