package com.eilas.newcourseschedule.ui.login

import android.content.Context
import android.view.View
import com.eilas.newcourseschedule.data.login
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.google.android.material.snackbar.Snackbar

fun saveUser(context: Context, user: LoggedInUser) {
    Thread {
        context.getSharedPreferences("user", Context.MODE_PRIVATE).edit()
            .putString("id", user.id).putString("pwd", user.pwd).putString("name", user.name)
            .putString("sex", user.sex.toString()).apply()
    }.start()
}

fun loadUser(context: Context): LoggedInUser? {
    return context.getSharedPreferences("user", Context.MODE_PRIVATE).let {
        val id = it.getString("id", null)
        val pwd = it.getString("pwd", null)
        val name = it.getString("name", null)
        val sex = if (LoggedInUser.Sex.MALE.equals(it.getString("sex", null)))
            LoggedInUser.Sex.MALE
        else
            LoggedInUser.Sex.FEMALE

        if (id != null && pwd != null)
            LoggedInUser(id, pwd, name.toString(), sex)
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

fun autoLogin(context: Context, view: View) {
    loadUser(context)?.apply {
        Snackbar.make(view, "自动登录中...", Snackbar.LENGTH_LONG).show()
        login(this)
        (context as LoginActivity).loggedInUser = this
    }
}