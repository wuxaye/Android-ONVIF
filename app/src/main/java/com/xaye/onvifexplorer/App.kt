package com.xaye.onvifexplorer

import android.app.Application
import com.xaye.helper.base.DevHelper

/**
 * Author xaye
 * @date: 2024/12/23
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DevHelper.init(this, BuildConfig.DEBUG)
    }
}