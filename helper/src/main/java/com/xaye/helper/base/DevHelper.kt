package com.xaye.helper.base

import android.app.Application
import android.os.Environment
import android.view.Gravity
import com.hjq.toast.ToastUtils
import com.xaye.helper.loadsir.callback.SuccessCallback
import com.xaye.helper.loadsir.core.LoadSir
import com.xaye.helper.util.KtxActivityLifecycleCallbacks
import com.xaye.helper.widget.state.BaseEmptyCallback
import com.xaye.helper.widget.state.BaseErrorCallback
import com.xaye.helper.widget.state.BaseLoadingCallback
import com.xaye.helper.ext.dp
import com.xaye.helper.ext.logI
import com.xaye.helper.util.mvvmHelperLog
import java.io.File

/**
 * Author xaye
 * @date: 2024/6/12
 */

/**
 * 全局上下文，可直接拿
 */
val appContext: Application by lazy { DevHelper.app }
object DevHelper {
    lateinit var app: Application

    /**
     * 框架初始化
     * @param application Application 全局上下文
     * @param debug Boolean  true为debug模式，会打印Log日志 false 关闭Log日志
     */
    fun init(application: Application, debug: Boolean) {
        app = application
        mvvmHelperLog = debug
        //注册全局 activity生命周期监听
        application.registerActivityLifecycleCallbacks(KtxActivityLifecycleCallbacks())
        LoadSir.beginBuilder()
            .setErrorCallBack(BaseErrorCallback())
            .setEmptyCallBack(BaseEmptyCallback())
            .setLoadingCallBack(BaseLoadingCallback())
            .setDefaultCallback(SuccessCallback::class.java)
            .commit()
        ToastUtils.init(app)
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 100.dp)
    }


}