package com.xaye.onvifexplorer.adapter

import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xaye.helper.ext.clickNoRepeat
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import com.xaye.onvifdiscoverykit.utils.Logger
import com.xaye.onvifexplorer.R

/**
 * Author xaye
 * @date: 2024/12/25
 */
class DevicesAdapter(
    private val devices: List<OnvifDevice>,
    private val onItemClick: (OnvifDevice) -> Unit,
    private val onItemPlayClick : (OnvifDevice) -> Unit
) : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    // ViewHolder 内部类
    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val tvDeviceIP: TextView = itemView.findViewById(R.id.tvDeviceIP)
        val ivArrow: ImageView = itemView.findViewById(R.id.ivArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.tvDeviceName.text = if (device.manufacturer.isNullOrEmpty()) "未知设备" else device.manufacturer
        holder.tvDeviceIP.text = device.ipAddress

        // 设置点击事件
        holder.itemView.clickNoRepeat {
            onItemClick(device)
        }

        holder.ivArrow.clickNoRepeat {
            Logger.d("点击了播放按钮: ${device.ipAddress}")
            onItemPlayClick(device)
        }
    }

    override fun getItemCount(): Int = devices.size
}