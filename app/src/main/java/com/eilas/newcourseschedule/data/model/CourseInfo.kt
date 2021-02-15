package com.eilas.newcourseschedule.data.model

import java.util.*

//一个courseinfo与一连串gridview的item对应
data class CourseInfo(
    val courseName: String,
    val courseStrTime1: Date,
    val courseEndTime1: Date,
    val courseStrTime2: Date? = null,
    val courseEndTime2: Date? = null,
    val lastWeek: Int = 0,
    val info: String = "",
    val courseItemIndexList: ArrayList<CourseItemIndex>
)