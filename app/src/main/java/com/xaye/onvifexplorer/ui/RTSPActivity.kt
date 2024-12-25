package com.xaye.onvifexplorer.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import com.alexvas.rtsp.widget.RtspStatusListener
import com.hjq.toast.ToastUtils
import com.xaye.helper.base.BaseViewModel
import com.xaye.helper.ext.clickNoRepeat
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import com.xaye.onvifdiscoverykit.utils.Logger
import com.xaye.onvifexplorer.R
import com.xaye.onvifexplorer.base.BaseActivity
import com.xaye.onvifexplorer.databinding.ActivityRtspactivityBinding
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

class RTSPActivity : BaseActivity<BaseViewModel, ActivityRtspactivityBinding>() {
    private val TAG = "RTSPActivity"


    private var mainStreamUrl: String? = null
    private var subStreamUrl: String? = null

    private var currentStream: String? = mainStreamUrl

    private var statisticsTimer: Timer? = null
    private var svVideoSurfaceResolution = Pair(0, 0)

    private var device: OnvifDevice? = null


    override fun initView(savedInstanceState: Bundle?) {

        device = intent.getParcelableExtra("DEVICE_DATA")

        if (device != null) {
            // 使用接收到的 OnvifDevice 对象
            mainStreamUrl = device?.profiles?.firstOrNull()?.rtspUrl
            subStreamUrl = device?.profiles?.getOrNull(1)?.rtspUrl
            Logger.d("mainStreamUrl: $mainStreamUrl subStreamUrl: $subStreamUrl ")
        } else {
            Logger.e("No device data received!")
        }

        mBind.svVideoSurface.setStatusListener(rtspStatusSurfaceListener)
    }

    override fun onBindViewClick() {
        mBind.bnStartStopSurface.clickNoRepeat {
            val selectedStreamId = mBind.rgStreamSelection.checkedRadioButtonId
            currentStream = if (selectedStreamId == R.id.rbMainStream) {
                mainStreamUrl
            } else {
                subStreamUrl
            }
            startPlayback()
        }

        mBind.bnSnapshotSurface.setOnClickListener {
            val bitmap = getSnapshot()
            if (bitmap != null) {
                ToastUtils.show("抓拍成功")
            } else {
                ToastUtils.show("抓拍失败")
            }
        }

        mBind.btnBack.clickNoRepeat {
            finish()
        }
    }

    private fun startPlayback() {
        device?.apply {
            if (mBind.svVideoSurface.isStarted()) {
                mBind.svVideoSurface.stop()
                stopStatistics()
            } else {
                val uri = Uri.parse(currentStream)
                mBind.svVideoSurface.apply {
                    init(
                        uri,
                        username = userName,
                        password = psw,
                        userAgent = "rtsp-client-android"
                    )
                    start(
                        requestVideo = true,
                        requestAudio = false,
                        requestApplication = true
                    )
                }
                startStatistics()
            }
        }

    }

    private fun startStatistics() {
        Logger.d("startStatistics()")
        Log.i(TAG, "Start statistics")
        if (statisticsTimer == null) {
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    val statistics = mBind.svVideoSurface.statistics
                    val text =
                        "Video decoder: ${
                            statistics.videoDecoderType.toString().lowercase()
                        } ${if (statistics.videoDecoderName.isNullOrEmpty()) "" else "(${statistics.videoDecoderName})"}" +
                                "\nVideo decoder latency: ${statistics.videoDecoderLatencyMsec} ms" +
                                "\nResolution: ${svVideoSurfaceResolution.first}x${svVideoSurfaceResolution.second}"

                    mBind.tvStatistics.post {
                        mBind.tvStatistics.text = text
                    }
                }
            }
            statisticsTimer = Timer("${TAG}::Statistics").apply {
                schedule(task, 0, 1000)
            }
        }
    }

    private fun stopStatistics() {
        Logger.d("stopStatistics()")
        statisticsTimer?.apply {
            Log.i(TAG, "Stop statistics")
            cancel()
        }
        statisticsTimer = null
    }

    private val rtspStatusSurfaceListener = object : RtspStatusListener {
        override fun onRtspStatusConnecting() {
            Logger.d("onRtspStatusConnecting()")
            mBind.apply {
                tvStatusSurface.text = "RTSP connecting..."
                pbLoadingSurface.visibility = View.VISIBLE
                vShutterSurface.visibility = View.VISIBLE
                setRadioGroupEnabled(enabled = false)
            }
        }

        override fun onRtspStatusConnected() {
            Logger.d("onRtspStatusConnected()")
            mBind.apply {
                tvStatusSurface.text = "RTSP connected"
                bnStartStopSurface.text = "Stop RTSP"
            }
            setKeepScreenOn(true)
        }

        override fun onRtspStatusDisconnecting() {
            Logger.d("onRtspStatusDisconnecting()")
            mBind.apply {
                tvStatusSurface.text = "RTSP disconnecting"
            }
        }

        override fun onRtspStatusDisconnected() {
            Logger.d("onRtspStatusDisconnected()")
            mBind.apply {
                tvStatusSurface.text = "RTSP disconnected"
                bnStartStopSurface.text = "Start RTSP"
                pbLoadingSurface.visibility = View.GONE
                vShutterSurface.visibility = View.VISIBLE
                pbLoadingSurface.isEnabled = false
                setRadioGroupEnabled(enabled = true)
            }
            setKeepScreenOn(false)
        }

        override fun onRtspStatusFailedUnauthorized() {
            Logger.e("onRtspStatusFailedUnauthorized()")
            if (isDestroyed) return
            onRtspStatusDisconnected()
            mBind.apply {
                tvStatusSurface.text = "RTSP username or password invalid"
                pbLoadingSurface.visibility = View.GONE
                setRadioGroupEnabled(enabled = true)
            }
        }

        override fun onRtspStatusFailed(message: String?) {
            Logger.e("onRtspStatusFailed(message='$message')")
            if (isDestroyed) return
            onRtspStatusDisconnected()
            mBind.apply {
                tvStatusSurface.text = "Error: $message"
                pbLoadingSurface.visibility = View.GONE
                setRadioGroupEnabled(enabled = true)
            }
        }

        override fun onRtspFirstFrameRendered() {
            Logger.d("onRtspFirstFrameRendered()")
            Log.i(TAG, "First frame rendered")
            mBind.apply {
                pbLoadingSurface.visibility = View.GONE
                vShutterSurface.visibility = View.GONE
                bnSnapshotSurface.isEnabled = true
            }
        }

        override fun onRtspFrameSizeChanged(width: Int, height: Int) {
            Logger.d("onRtspFrameSizeChanged(width=$width, height=$height)")
            Log.i(TAG, "Video resolution changed to ${width}x${height}")
            svVideoSurfaceResolution = Pair(width, height)
        }
    }

    private fun setRadioGroupEnabled(enabled: Boolean) {
        for (i in 0 until mBind.rgStreamSelection.childCount) {
            val child = mBind.rgStreamSelection.getChildAt(i)
            if (child is RadioButton) {
                child.isEnabled = enabled
            }
        }
    }

    private fun getSnapshot(): Bitmap? {
        Logger.d("getSnapshot()")
        val surfaceBitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)
        val lock = Object()
        val success = AtomicBoolean(false)
        val thread = HandlerThread("PixelCopyHelper")
        thread.start()
        val sHandler = Handler(thread.looper)
        val listener = PixelCopy.OnPixelCopyFinishedListener { copyResult ->
            success.set(copyResult == PixelCopy.SUCCESS)
            synchronized(lock) {
                lock.notify()
            }
        }
        synchronized(lock) {
            PixelCopy.request(
                mBind.svVideoSurface.holder.surface,
                surfaceBitmap,
                listener,
                sHandler
            )
            lock.wait()
        }
        thread.quitSafely()
        return if (success.get()) surfaceBitmap else null
    }

    private fun setKeepScreenOn(enable: Boolean) {
        Logger.d("setKeepScreenOn(enable=$enable")
        if (enable) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.i(TAG, "Enabled keep screen on")
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.i(TAG, "Disabled keep screen on")
        }
    }

    override fun onPause() {
        val started = mBind.svVideoSurface.isStarted()

        Logger.d("onPause(), started:$started")
        super.onPause()

        if (started) {
            mBind.svVideoSurface.stop()
            stopStatistics()
        }
    }
}