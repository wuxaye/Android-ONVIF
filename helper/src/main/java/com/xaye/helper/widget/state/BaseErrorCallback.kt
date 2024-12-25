package com.xaye.helper.widget.state

import com.xaye.helper.R
import com.xaye.helper.loadsir.callback.Callback


/**
 * 作者　: hegaojian
 * 时间　: 2021/6/8
 * 描述　:
 */
class BaseErrorCallback : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_base_error
    }

}