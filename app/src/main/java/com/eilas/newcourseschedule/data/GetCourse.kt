package com.eilas.newcourseschedule.data

import android.os.Handler
import android.os.Message
import android.util.Log
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//查询一周的所有课程,需要单双周信息->"第n周"
fun getAllCourse(user: LoggedInUser, firstWeek: Calendar, handler: Handler) {
    Thread {
        val courseList = ArrayList<CourseInfo>()
        val httpHelper = HttpHelper.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                Gson().toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/course?action=search&all=true&week=${getThisWeek(firstWeek)}")
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
//                Log.i("response", response.body?.string())
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                JsonParser().parse(response.body?.string()).asJsonArray.forEach {
                    courseList.add(it.asJsonObject.let {
                        CourseInfo(
                            it["name"].asString,
                            simpleDateFormat.parse(it["strTime1"].asString),
                            simpleDateFormat.parse(it["endTime1"].asString),
                            null,
                            null,
                            0,
                            0,
                            null.toString(),
                            it["location"].asString,
                            it["id"].asString
                        )
                    })
                }

                handler.sendMessage(Message.obtain().apply {
                    what = 1
                    this.obj = courseList
                })
            }

        })

        httpHelper.recycle()
    }.start()
}

//查看单个课程信息
/*fun getSingleCourse(user: LoggedInUser, course: CourseInfo): CourseInfo {

}*/

fun saveCourse(user: LoggedInUser, course: CourseInfo, handler: Handler) {
    Thread {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        val httpHelper = HttpHelper.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(mapOf("user" to user, "course" to course))
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/course?action=save").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
                Log.i("saveCourse response", response.body?.string())
                handler.sendMessage(Message.obtain().apply { what = 2 })
/*
                JsonParser().parse(response.body?.string()).asJsonObject.let {

                }
*/
            }

        })

        httpHelper.recycle()
    }.start()
}

fun dropCourse(user: LoggedInUser, course: CourseInfo, handler: Handler) {
    Thread {
        val httpHelper = HttpHelper.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                Gson().toJson(mapOf("user" to user, "course" to course))
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url("${httpHelper.url}/course?action=drop").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("dropCourse response", response.body?.string())

                handler.sendMessage(Message.obtain().apply { what = 99 })
            }

        })
    }.start()
}
