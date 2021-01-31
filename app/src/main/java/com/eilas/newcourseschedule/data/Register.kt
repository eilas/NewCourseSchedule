package com.eilas.newcourseschedule.data

import android.os.Message
import android.util.Log
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.ui.login.LoginActivity
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

fun register(user: LoggedInUser): Result<LoggedInUser> {
    val gson = Gson()
    try {
//        发送注册信息
        OkHttpClient().newCall(
            Request.Builder().post(
                gson.toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url("http://192.168.0.107:8080/register").build()
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

//        需要这个吗
        return Result.success(user)
    } catch (e: Throwable) {
        return Result.failure(IOException("Error register in", e))
    }
}

