package com.xaye.onvifdiscoverykit.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Author xaye
 * @date: 2024/12/23
 */
@Parcelize
data class NetworkInterface (
    // 标识网络接口的令牌，用于唯一标识接口
    var interfaceToken: String? = null,

    // 最大传输单元
    var mtu: String? = null,

    // IPv6前缀长度
    var ipvtPrefixLength: String? = null,
) : Parcelable