package com.eilas.newcourseschedule.data.model

//一个courseinfo与一连串gridview的item对应
data class CourseInfo(
    val courseName: String = "",
    val courseStrTime: String = "",
    val courseEndTime: String = "",
    val lastWeek: Int = 0,
    val isEmpty: Boolean = courseName == null
)