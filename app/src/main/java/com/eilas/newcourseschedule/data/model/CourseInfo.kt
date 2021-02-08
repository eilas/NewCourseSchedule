package com.eilas.newcourseschedule.data.model

data class CourseInfo(
    val courseName: String? = null,
    val courseStrTime: String? = null,
    val courseEndTime: String? = null,
    val isEmpty: Boolean = courseName == null
)