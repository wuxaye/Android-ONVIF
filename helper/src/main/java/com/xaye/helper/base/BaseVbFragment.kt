package com.xaye.helper.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.xaye.helper.ext.inflateBinding

/**
 * Author xaye
 * @date: 2024-06-13 14:27
 */
abstract class BaseVbFragment<VM : BaseViewModel, VB : ViewBinding> : BaseVmFragment<VM>(), BaseIView  {

    //使用了 ViewBinding 就不需要 layoutId了，因为 会从 VB 泛型 找到相关的view
    override val layoutId: Int = 0
    private var _binding: VB? = null
    val mBind: VB get() = _binding!!

    /**
     * 创建 ViewBinding
     */
    override fun initViewDataBind(inflater: LayoutInflater, container: ViewGroup?): View? {
        _binding = inflateBinding(inflater, container, false)
        return mBind.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}