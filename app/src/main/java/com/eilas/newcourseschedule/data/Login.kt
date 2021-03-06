package com.eilas.newcourseschedule.data

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.ui.LoginActivity
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun login(user: LoggedInUser, handler: Handler) {
    Thread {
        val gson = Gson()
        val httpHelper = HttpHelpers.obtain()
//        发送登录信息
        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/login").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.sendMessage(Message.obtain().apply { what = 6 })
            }

            override fun onResponse(call: Call, response: Response) {
//                区分登录情况
                val jsonObject = JsonParser().parse(response.body?.string()).asJsonObject
                jsonObject["result"].asString.let {
                    handler.sendMessage(Message.obtain().apply {
                        when (it) {
                            "OK" -> {
                                what = 3
                                obj = jsonObject["name"].asString
                            }
                            "pwdError" -> what = 4
                            "noUser" -> what = 5
                            else -> throw Exception("未知错误")
                        }
                    })
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
