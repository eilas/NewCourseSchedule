package com.eilas.newcourseschedule.ui.login

import android.content.Context
import com.eilas.newcourseschedule.data.login
import com.eilas.newcourseschedule.data.model.LoggedInUser

fun saveUser(context: Context, user: LoggedInUser) {
    Thread {
        context.getSharedPreferences("user", Context.MODE_PRIVATE).edit()
            .putString("id", user.id).putString("pwd", user.pwd).apply()
    }.start()
}

fun loadUser(context: Context): LoggedInUser? {
    return context.getSharedPreferences("user", Context.MODE_PRIVATE).let {
        val id = it.getString("id", null)
        val pwd = it.getString("pwd", null)
        if (id != null && pwd != null)
            LoggedInUser(id, pwd)
        else
            null
    }
}

fun deleteUser(context: Context): Boolean {
    Thread {
        context.getSharedPreferences("user", Context.MODE_PRIVATE).edit().clear().apply()
    }.start()
    return true
}

fun autoLogin(context: Context) {
    loadUser(context)?.let {
//        自动登录在主线程请求
        login(it)
        (context as LoginActivity).loggedInUser = it
    }
}