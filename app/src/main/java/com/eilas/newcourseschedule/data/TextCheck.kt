package com.eilas.newcourseschedule.data

import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

fun textIsNotEmpty(vararg textInputLayouts: TextInputLayout, unclickedView: View? = null) {
    unclickedView?.isEnabled = false
    textInputLayouts.forEach { input ->
        input.editText?.doAfterTextChanged {
            if (it!!.trim().isEmpty()) {
                input.error = "不能为空！"
                unclickedView?.isEnabled = false
            } else {
                input.error = null
                if (textInputLayouts.all {
                        it.error.isNullOrEmpty() && it.editText!!.text.trim().isNotEmpty()
                    }) {
                    unclickedView?.isEnabled = true
                }
            }
        }
    }
}

fun textIsNumber(vararg textInputLayouts: TextInputLayout) {
    textInputLayouts.forEach { input ->
        input.editText?.doAfterTextChanged {
            runCatching { it!!.trim().toString().toInt() }
                .onFailure { input.error = "必须为数字！" }
                .onSuccess { input.error = null }
        }
    }
}