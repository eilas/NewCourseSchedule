package com.eilas.newcourseschedule.data.model

//CourseItemIndex与CourseInfo相互包含
data class CourseItemIndex(val indexI: Int, val indexJ: Int, val courseInfo: CourseInfo?) {
    fun isEmpty(): Boolean = courseInfo == null
}