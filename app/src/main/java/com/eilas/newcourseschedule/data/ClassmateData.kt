package com.eilas.newcourseschedule.data

import android.os.Handler
import android.os.Message
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.data.model.User
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun getClassmate(user: LoggedInUser, courseId: String, handler: Handler) {
    Thread {

        val httpHelper = HttpHelpers.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                Gson().toJson(mapOf("user" to user, "courseId" to courseId))
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url("${httpHelper.url}/user?action=search").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val classmateList = JsonParser().parse(response.body?.string()).asJsonArray.map {
                    it.asJsonObject.let {
/*
                        OtherUser(
                            it["id"].asString,
                            it["name"].asString,
                            if (it["sex"].asString.equals(User.Sex.MALE)) User.Sex.MALE else User.Sex.FEMALE
                        )
*/
                        Triple(
                            it["id"].asString,
                            it["name"].asString,
                            if (User.Sex.valueOf(it["sex"].asString)
                                    .equals(User.Sex.MALE)
                            ) "男" else "女"
                        )

                    }
                }

                handler.sendMessage(Message.obtain().apply {
                    what = 4
                    obj = classmateList
                })
            }
        })

        httpHelper.recycle()
    }.start()
}