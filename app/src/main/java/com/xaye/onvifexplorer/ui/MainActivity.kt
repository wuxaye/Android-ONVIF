package com.xaye.onvifexplorer.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.toast.ToastUtils
import com.xaye.helper.base.BaseViewModel
import com.xaye.helper.ext.clickNoRepeat
import com.xaye.helper.ext.gone
import com.xaye.helper.ext.visible
import com.xaye.onvifdiscoverykit.OnvifClient
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import com.xaye.onvifdiscoverykit.callback.DeviceDiscoveryCallback
import com.xaye.onvifexplorer.adapter.DevicesAdapter
import com.xaye.onvifexplorer.base.BaseActivity
import com.xaye.onvifexplorer.databinding.ActivityMainBinding
import com.xaye.onvifexplorer.ui.dialog.OnvifDeviceDetailsDialogFragment

class MainActivity : BaseActivity<BaseViewModel, ActivityMainBinding>() {
    private val TAG = "MainActivity"

    private val devicesList = mutableListOf<OnvifDevice>()
    private lateinit var devicesAdapter: DevicesAdapter
    override fun initView(savedInstanceState: Bundle?) {

        devicesAdapter = DevicesAdapter(
            devices = devicesList,
            onItemClick = { device ->
                // 设备点击的处理
                val dialogFragment = OnvifDeviceDetailsDialogFragment.newInstance(device)
                dialogFragment.show(supportFragmentManager, "OnvifDeviceDetailsDialog")
            },
            onItemPlayClick = { device ->
                // 播放按钮点击的处理
                val intent = Intent(this, RTSPActivity::class.java).apply {
                    putExtra("DEVICE_DATA", device)
                }
                startActivity(intent)
            }
        )
        mBind.recyclerViewDevices.layoutManager = LinearLayoutManager(this)
        mBind.recyclerViewDevices.adapter = devicesAdapter
    }

    override fun onBindViewClick() {
        mBind.btnStartSearch.clickNoRepeat {
            startDeviceSearch()
        }

        mBind.btnOneClickConfigure.clickNoRepeat {
            mBind.apply {
                val manufacturer = etManufacturer.text.toString().trim()
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()

                OnvifClient.setCredentials(manufacturer, username, password)
                ToastUtils.show("设置成功")
            }
        }
    }

    private fun startDeviceSearch() {
        // 启动设备搜索
        OnvifClient.startDiscovery(this, object : DeviceDiscoveryCallback {
            override fun onSearchStarted() {
                runOnUiThread {
                    mBind.progressBar.visible()
                    mBind.btnStartSearch.isEnabled = false
                    mBind.tvNoDevices.gone()
                    devicesList.clear()
                    devicesAdapter.notifyDataSetChanged()
                }
            }

            override fun onDeviceFound(device: OnvifDevice) {
                runOnUiThread {
                    if (!devicesList.contains(device)) {
                        devicesList.add(device)
                        devicesAdapter.notifyItemInserted(devicesList.size - 1)
                    }
                }
            }

            override fun onSearchFinished() {
                runOnUiThread {
                    mBind.progressBar.gone()
                    mBind.btnStartSearch.isEnabled = true
                    if (devicesList.isEmpty()) {
                        mBind.tvNoDevices.visible()
                    } else {
                        mBind.tvNoDevices.gone()
                    }
                }
            }

            override fun onSearchFailed(e: Throwable) {
                runOnUiThread {
                    mBind.progressBar.gone()
                    mBind.btnStartSearch.isEnabled = true
                    mBind.tvNoDevices.visible()
                    mBind.tvNoDevices.text = "搜索失败: ${e.message}"
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        OnvifClient.stopDiscovery()
    }

}