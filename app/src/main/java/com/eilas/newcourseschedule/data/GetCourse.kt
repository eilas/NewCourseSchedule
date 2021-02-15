package com.eilas.newcourseschedule.data

import android.util.Log
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

//查询一周的所有课程,需要单双周信息->"第n周"
fun getAllCourse(user: LoggedInUser): ArrayList<CourseItemIndex> {
    val courseList = ArrayList<CourseItemIndex>()
    // TODO: 2021/2/10 获取当前周
    Thread {
        val httpHelper = HttpHelper.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                Gson().toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/course?search=true&all=true&week=1").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
                Log.i("response", response.body?.string())
/*
                JsonParser().parse(response.body?.string()).asJsonObject.let {
                    Log.i("course", it.asString)
                }
*/
            }

        })

        httpHelper.recycle()
    }.start()
    return courseList
}

//查看单个课程信息
/*fun getSingleCourse(user: LoggedInUser, course: CourseInfo): CourseInfo {

}*/

fun saveCourse(user: LoggedInUser, course: CourseInfo) {

    Thread {
        val gson = Gson()
        val httpHelper = HttpHelper.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(
                    JsonParser().parse(gson.toJson(course)).asJsonObject.addProperty(
                        "id",
                        user.id
                    )
                )
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/course").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
                JsonParser().parse(response.body?.string()).asJsonObject.let {

                }
            }

        })

        httpHelper.recycle()
    }.start()
}
