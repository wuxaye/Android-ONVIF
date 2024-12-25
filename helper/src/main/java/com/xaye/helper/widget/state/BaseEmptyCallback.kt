package com.xaye.helper.widget.state

import com.xaye.helper.R
import com.xaye.helper.loadsir.callback.Callback


/**
 * 作者　: hegaojian
 * 时间　: 2020/12/14
 * 描述　:
 */
class BaseEmptyCallback() : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_base_empty
    }

}