package com.eilas.newcourseschedule.data

import android.util.Log
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

/**
 * 输入检查类,用于检查。
 * 在构造函数中指定需要检查的全体。
 * 使用textIsNotEmpty和/或textIsNumber指定检查规则和具体需要检查的输入
 * @param allTextInputLayouts 需要检查的全体
 */
class TextCheck(vararg allTextInputLayouts: TextInputLayout) {
    private val `layout-funcListMap` =
        HashMap<TextInputLayout, ArrayList<(TextInputLayout) -> Boolean>>()

    init {
        allTextInputLayouts.forEach { `layout-funcListMap`.put(it, ArrayList()) }
    }

    fun textIsNotEmpty(vararg textInputLayouts: TextInputLayout, unclickedView: View? = null) {
        unclickedView?.isEnabled = false
        textInputLayouts.forEach { input ->
            input.editText?.doAfterTextChanged {
                doAll(input, `layout-funcListMap`[input]!!)
                checkAll(unclickedView = unclickedView)
            }
            `layout-funcListMap`[input]!!.add { inputLayout ->
                inputLayout.error =
                    if (inputLayout.editText?.text?.trim().toString().isEmpty()) "不能为空！" else null
                true
            }
        }
    }

    fun textIsNumber(vararg textInputLayouts: TextInputLayout, unclickedView: View? = null) {
        unclickedView?.isEnabled = false
        textInputLayouts.forEach { input ->
            input.editText?.doAfterTextChanged {
                doAll(input, `layout-funcListMap`[input]!!)
                checkAll(unclickedView = unclickedView)
            }
            `layout-funcListMap`[input]!!.add { inputLayout ->
                runCatching { inputLayout.editText?.text?.trim().toString().toInt() }
                    .onFailure { inputLayout.error = "必须为数字！" }
                    .onSuccess { inputLayout.error = null }
                true
            }
        }
    }

    private fun doAll(
        inputLayout: TextInputLayout,
        functionList: List<(TextInputLayout) -> Boolean>,
    ) {
        for (func in functionList) {
            func.invoke(inputLayout)
            if (inputLayout.error != null) break
        }
    }

    /**
     * 检查规则。
     * 输入框无error && 输入非空。
     */
    private fun checkAll(
        textInputLayouts: Array<out TextInputLayout>? = null,
        unclickedView: View? = null
    ) {
        unclickedView?.isEnabled =
            `layout-funcListMap`.keys.all {
                it.error == null && it.editText!!.text.trim().isNotEmpty()
            }
        Log.i("text check", unclickedView?.isEnabled.toString())
    }
}