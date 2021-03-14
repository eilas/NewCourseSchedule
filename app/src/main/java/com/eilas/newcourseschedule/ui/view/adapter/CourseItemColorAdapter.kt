package com.eilas.newcourseschedule.ui.view.adapter

import android.content.Context
import com.alamkanak.weekview.WeekViewEvent

class CourseItemColorAdapter(val context: Context, val weekViewEventList: List<WeekViewEvent>) {
    companion object {
        private val colorList: ArrayList<Int> = ArrayList()
        private val colorUsed: HashMap<Int, Boolean> = HashMap()
        private val colorMap: HashMap<String, Int> = HashMap()
    }

    init {
        val resources = context.resources
        val packageName = context.packageName
        println(packageName)
        colorList.apply {
            for (i in 1..16) {
                add(
                    resources.getColor(
                        resources.getIdentifier(
                            "course_color_$i",
                            "color",
                            packageName
                        )
                    )
                )
            }

            /*add(resources.getColor(R.color.course_color_1))
            add(resources.getColor(R.color.course_color_2))
            add(resources.getColor(R.color.course_color_3))
            add(resources.getColor(R.color.course_color_4))
            add(resources.getColor(R.color.course_color_5))
            add(resources.getColor(R.color.course_color_6))
            add(resources.getColor(R.color.course_color_7))
            add(resources.getColor(R.color.course_color_8))
            add(resources.getColor(R.color.course_color_9))
            add(resources.getColor(R.color.course_color_10))
            add(resources.getColor(R.color.course_color_11))
            add(resources.getColor(R.color.course_color_12))
            add(resources.getColor(R.color.course_color_13))
            add(resources.getColor(R.color.course_color_14))
            add(resources.getColor(R.color.course_color_15))
            add(resources.getColor(R.color.course_color_16))*/
        }
    }

    fun bindColor() {
        var j = 0
        for (i in weekViewEventList.indices) {
            if (i != 0 && weekViewEventList[i].id.toString() == weekViewEventList[i - 1].id.toString()) {
                continue
            }
            colorUsed.put(colorList[j], true)
            colorMap.put(weekViewEventList[i].id.toString(), colorList[j++])
        }
        while (j in colorList.indices) {
            colorUsed.put(colorList[j++], false)
        }

        apply()
    }

    fun refresh() {
        val weekViewEventIdSet = HashSet<String>()
        weekViewEventList.mapNotNullTo(weekViewEventIdSet, { it.id })
        val death = colorMap.keys.filter { !weekViewEventIdSet.contains(it) }//已删除课程id
        val born = weekViewEventIdSet.filter { !colorMap.keys.contains(it) }//新加入课程id
        death.forEach {
            colorUsed.set(colorMap[it]!!, false)
            colorMap.remove(it)
        }
        born.forEach {
            for ((color, used) in colorUsed) {
                if (!used) {
                    colorUsed.set(color, true)
                    colorMap.put(it, color)
                    break
                }
            }
        }

        apply()
    }

    private fun apply() {
        weekViewEventList.forEach {
            it.color= colorMap[it.id]!!
        }
//        colorMap.forEach { (event, color) -> event.color = color }
    }
}