package com.eilas.newcourseschedule.data.model

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

data class CourseInfo(
    val courseName: String,
    val courseStrTime1: Date,
    val courseEndTime1: Date,
    val courseStrTime2: Date? = null,
    val courseEndTime2: Date? = null,
    val strWeek: Int,
    val lastWeek: Int,
    val info: String = "",
    val location: String,
    val id: String = ""
) {
    fun getCourseStrTimeToday(): Date? =
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            courseStrTime1.day + 1 -> courseStrTime1
            courseStrTime2 ?: Int.MIN_VALUE + 1 -> courseStrTime2
            else -> null
        }
}

fun main() {
    val courseInfo = CourseInfo(
        courseName = "test",
        courseStrTime1 = Calendar.getInstance().time,
        courseEndTime1 = Calendar.getInstance().time,
        strWeek = 1,
        lastWeek = 2,
        info = "info_test",
        location = "江苏"
    ).apply {
        val regex = Regex("(\"course)(Str|End)(Time)([0-9])(\":\")(.*?)(\",+)")
        val toJson = Gson().toJson(this).replace(regex, {
            it.groupValues[1] + it.groupValues[2] + it.groupValues[3] + it.groupValues[4] + it.groupValues[5] + SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss"
            ).format(
                if (it.groupValues[2] == "Str")
                    if (it.groupValues[4] == "1")
                        courseStrTime1
                    else
                        courseStrTime2
                else
                    if (it.groupValues[4] == "1")
                        courseEndTime1
                    else
                        courseEndTime2
            ) + it.groupValues[7]
        })
        println(toJson)
    }
}