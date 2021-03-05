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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//查询一周的所有课程,需要单双周信息->"第n周"
fun getAllCourse(user: LoggedInUser, firstWeek: Calendar): ArrayList<CourseItemIndex> {
    val courseList = ArrayList<CourseItemIndex>()

    Thread {
        val httpHelper = HttpHelper.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                Gson().toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/course?search=true&all=true&week=${getThisWeek(firstWeek)}")
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
                Log.i("response", response.body?.string())
/*
                JsonParser().parse(response.body?.string()).asJsonObject.let {
                    Log.i("response", response.body?.string())
                }
*/
            }

        })

        httpHelper.recycle()
    }.start()
    return courseList.apply {
        for (time in 0 until 12)
            for (day in 0 until 7)
                add(CourseItemIndex(time, day, null))
    }
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
                gson.toJson(HashMap<String, Any>().apply {
                    put("user", user)
                    put("course", course)
                }).replace(Regex("(\"course)(Str|End)(Time)([0-9])(\":\")(.*?)(\",+)"), {
//                    将json字符串中的时间转换成yyyy-MM-dd HH:mm:ss格式
                    it.groupValues[1] + it.groupValues[2] + it.groupValues[3] + it.groupValues[4] +
                            it.groupValues[5] + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                        if (it.groupValues[2] == "Str")
                            if (it.groupValues[4] == "1")
                                course.courseStrTime1
                            else
                                course.courseStrTime2
                        else
                            if (it.groupValues[4] == "1")
                                course.courseEndTime1
                            else
                                course.courseEndTime2
                    ) + it.groupValues[7]
                }).apply {
                    Log.i("user+course", this)
                }.toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/course?search=false").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                // TODO: 2021/2/9 考虑单双周
                Log.i("saveCourse response", response.body?.string())
/*
                JsonParser().parse(response.body?.string()).asJsonObject.let {

                }
*/
            }

        })

        httpHelper.recycle()
    }.start()
}
