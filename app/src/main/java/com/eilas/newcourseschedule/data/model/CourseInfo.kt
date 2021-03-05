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
    val location:String
) {
/*
    override fun toString(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return "CourseInfo(courseName=$courseName, courseStrTime1=${
            simpleDateFormat.format(courseStrTime1)
        }, courseEndTime1=${simpleDateFormat.format(courseEndTime1)}, courseStrTime2=${
            courseStrTime2?.let { simpleDateFormat.format(it) }
        }, courseEndTime2=${courseEndTime2?.let { simpleDateFormat.format(it) }}, strWeek=$strWeek, lastWeek=$lastWeek, info=$info)"
    }
*/
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