package com.eilas.newcourseschedule.data

import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

//查询一周的所有课程,需要单双周信息->"第n周"
fun getAllCourse(user: LoggedInUser): ArrayList<CourseInfo> {
    // TODO: 2021/2/10 获取当前周
    Thread {
        HttpHelper.okHttpClient.newCall(
            Request.Builder().post(
                Gson().toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(HttpHelper.url + "/course?search=true&all=true&week=1").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
/*
                JsonParser().parse(response.body?.string()).asJsonObject.let {

                }
*/
            }

        })
    }.start()
    return ArrayList<CourseInfo>()
}

//查看单个课程信息
/*fun getSingleCourse(user: LoggedInUser, course: CourseInfo): CourseInfo {

}*/

fun saveCourse(user: LoggedInUser, course: CourseInfo) {
    val gson = Gson()
    Thread {
        OkHttpClient().newCall(
            Request.Builder().post(
                gson.toJson(
                    JsonParser().parse(gson.toJson(course)).asJsonObject.addProperty(
                        "id",
                        user.id
                    )
                )
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url("http://192.168.0.107:8080/course").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
                JsonParser().parse(response.body?.string()).asJsonObject.let {

                }
            }

        })
    }.start()
}
