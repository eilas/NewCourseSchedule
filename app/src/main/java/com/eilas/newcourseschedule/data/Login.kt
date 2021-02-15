package com.eilas.newcourseschedule.data

import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.ui.login.LoginActivity
import com.eilas.newcourseschedule.ui.login.deleteUser
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun login(user: LoggedInUser) {
    Thread {
        val gson = Gson()
        val httpHelper = HttpHelper.obtain()
//        发送登录信息
        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/login").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w("response", "failure!!!")
            }

            override fun onResponse(call: Call, response: Response) {
//                区分登录情况
//                Log.i("response", response.body?.string())
                JsonParser().parse(response.body?.string()).asJsonObject.get("result").asString.let {
                    when (it) {
                        "OK" -> {
                            LoginActivity.handle?.sendMessage(Message.obtain().let {
                                it.what = 3
                                it
                            })
                        }
                        "pwdError" -> {
                            LoginActivity.handle?.sendMessage(Message.obtain().let {
                                it.what = 4
                                it
                            })
                        }
                        "noUser" -> {
                            LoginActivity.handle?.sendMessage(Message.obtain().let {
                                it.what = 5
                                it
                            })
                        }
                        else -> throw Exception("似乎进入了未知领域...")
                    }
                }
            }

        })
        httpHelper.recycle()
    }.start()
}

fun logout(context: Context) {
    deleteUser(context)
    context.startActivity(
        Intent(
            context,
            LoginActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
