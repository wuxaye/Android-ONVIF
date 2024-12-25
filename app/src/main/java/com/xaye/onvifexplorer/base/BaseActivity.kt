package com.xaye.onvifexplorer.base

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.viewbinding.ViewBinding
import com.xaye.helper.base.BaseVBActivity
import com.xaye.helper.base.BaseViewModel
import com.xaye.helper.base.action.BundleAction
import com.xaye.helper.base.action.HandlerAction


/**
 * Author xaye
 * @date: 2024-06-13 14:15
 *
 * 需要自定义修改什么就重写什么 具体方法可以 搜索 BaseIView 查看
 */
abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : BaseVBActivity<VM, VB>(),
    BundleAction, HandlerAction {

    override fun getBundle(): Bundle? {
        return intent.extras
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideKeyboard(v, event)) {
                hideKeyboard(v)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 判断是否应该隐藏键盘
     */
    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val location = IntArray(2)
            v.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + v.getWidth()
            val bottom = top + v.getHeight()
            // 判断触摸点是否在输入框内
            if (event.rawX > left && event.rawX < right && event.rawY > top && event.rawY < bottom) {
                return false // 点击在输入框区域，不隐藏键盘
            }
        }
        return true // 点击在输入框外，隐藏键盘
    }

    /**
     * 隐藏键盘
     */
    private fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}