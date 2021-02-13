package com.eilas.newcourseschedule.data

import android.os.Message
import android.util.Log
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.ui.login.LoginActivity
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun register(user: LoggedInUser) {
    Thread {
        val gson = Gson()
        try {
//        发送注册信息
            HttpHelper.okHttpClient.newCall(
                Request.Builder().post(
                    gson.toJson(user)
                        .toRequestBody("application/json".toMediaTypeOrNull())
                ).url(HttpHelper.url + "/register").build()
            ).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.w("response", "failure!!!")
                }

                override fun onResponse(call: Call, response: Response) {
                    JsonParser().parse(response.body?.string()).asJsonObject.get("result").asString.let {
                        when (it) {
                            "OK" -> {
                                LoginActivity.handle?.sendMessage(Message.obtain().let {
                                    it.what = 3
                                    it
                                })
                            }
                            else -> throw Exception("似乎进入了未知领域...")
                        }
                    }
                }

            })
        } catch (e: Throwable) {
            TODO()
//            return Result.failure(IOException("Error register in", e))
        }

    }.start()
}

