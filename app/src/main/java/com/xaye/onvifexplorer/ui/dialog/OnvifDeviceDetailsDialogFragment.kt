package com.xaye.onvifexplorer.ui.dialog

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.xaye.onvifdiscoverykit.bean.OnvifDevice
import com.xaye.onvifexplorer.R

/**
 * Author xaye
 * @date: 2024/12/25
 */
class OnvifDeviceDetailsDialogFragment(private val device: OnvifDevice) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_device_details, container, false)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val containerLayout = view.findViewById<LinearLayout>(R.id.detailsContainer)

        // 动态添加设备的基本信息
        addDetailRow(containerLayout, "Manufacturer", device.manufacturer)
        addDetailRow(containerLayout, "Model", device.model)
        addDetailRow(containerLayout, "Serial Number", device.serialNumber)
        addDetailRow(containerLayout, "Firmware Version", device.firmwareVersion)
        addDetailRow(containerLayout, "UUID", device.uuid)
        addDetailRow(containerLayout, "IP Address", device.ipAddress)
        addDetailRow(containerLayout, "Media URL", device.mediaUrl)
        addDetailRow(containerLayout, "PTZ URL", device.ptzUrl)
        addDetailRow(containerLayout, "Image URL", device.imageUrl)
        addDetailRow(containerLayout, "Event URL", device.eventUrl)
        addDetailRow(containerLayout, "Analytics URL", device.analyticsUrl)

        // 动态添加 MediaProfiles 信息
        if (device.profiles.isNotEmpty()) {
            val title = TextView(requireContext()).apply {
                text = "Media Profiles"
                textSize = 18f
                setPadding(8, 16, 8, 8)
                setTypeface(typeface, Typeface.BOLD)
            }
            containerLayout.addView(title)

            device.profiles.forEachIndexed { index, profile ->
                val profileHeader = TextView(requireContext()).apply {
                    text = "Profile ${index + 1}: ${profile.name}"
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(8, 8, 8, 8)
                }
                containerLayout.addView(profileHeader)

                addDetailRow(containerLayout, "Token", profile.token)
                addDetailRow(containerLayout, "RTSP URL", profile.rtspUrl)
                addDetailRow(containerLayout, "Video Encoder", profile.videoEncode.toString())
                addDetailRow(containerLayout, "Audio Encoder", profile.audioEncode.toString())
                addDetailRow(containerLayout, "PTZ Configuration", profile.ptzConfiguration.toString())
            }
        }

        // 添加关闭按钮
        view.findViewById<Button>(R.id.btnClose)?.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * 动态添加一行信息
     */
    private fun addDetailRow(container: LinearLayout, key: String, value: String?) {
        if (!value.isNullOrEmpty()) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
            }

            val keyView = TextView(requireContext()).apply {
                text = "$key:"
                setTypeface(typeface, Typeface.BOLD)
                setPadding(8, 8, 8, 8)
            }

            val valueView = TextView(requireContext()).apply {
                text = value
                setPadding(8, 8, 8, 8)
            }

            row.addView(keyView)
            row.addView(valueView)
            container.addView(row)
        }
    }

    companion object {
        fun newInstance(device: OnvifDevice): OnvifDeviceDetailsDialogFragment {
            return OnvifDeviceDetailsDialogFragment(device)
        }
    }
}
