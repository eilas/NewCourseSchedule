package com.eilas.newcourseschedule.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

fun saveFirstWeek(context: Context, calendar: Calendar) {
    Thread {
        calendar.firstDayOfWeek = Calendar.MONDAY
        context.getSharedPreferences("date", Context.MODE_PRIVATE).edit()
            .putInt("year", calendar.get(Calendar.YEAR))
            .putInt("month", calendar.get(Calendar.MONTH))
            .putInt("weekOfMonth", calendar.get(Calendar.WEEK_OF_MONTH)).apply()
    }.start()
}

fun loadFirstWeek(context: Context): Calendar? {
    return context.getSharedPreferences("date", Context.MODE_PRIVATE).let {
        val year = it.getInt("year", -1)
        val month = it.getInt("month", -1)
        val weekOfMonth = it.getInt("weekOfMonth", -1)

        if (year != -1 && month != -1 && weekOfMonth != -1)
            Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.WEEK_OF_MONTH, weekOfMonth)
            }
        else
            null
    }
}

fun saveItemStrEndTime(context: Context, calendarMap: Map<String, Calendar>) {
    Thread {
        val edit = context.getSharedPreferences("date", Context.MODE_PRIVATE).edit()
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        calendarMap.forEach { (t, u) ->
            edit.putString(t, simpleDateFormat.format(u.time))
        }
        edit.apply()
    }.start()
}

fun loadItemStrEndTime(context: Context): Map<String, Calendar> {
    return context.getSharedPreferences("date", Context.MODE_PRIVATE).let {
        HashMap<String, Calendar>().apply {
            val simpleDateFormat = SimpleDateFormat("HH:mm")
//            一天13节，不能再多了...
            for (i in 0..12) {
                val strTime = it.getString("strTime$i", null)
                val endTime = it.getString("endTime$i", null)
                if (strTime != null && endTime != null) {
                    put("strTime$i", Calendar.getInstance().apply {
                        time = simpleDateFormat.parse(strTime)
                    })
                    put("endTime$i", Calendar.getInstance().apply {
                        time = simpleDateFormat.parse(endTime)
                    })
                } else
                    break
            }
        }
    }
}

//以第一周为准获取当前周数
fun getThisWeek(firstWeek: Calendar): Int {
    firstWeek.firstDayOfWeek = Calendar.MONDAY
    return Calendar.getInstance().let {
        it.firstDayOfWeek = Calendar.MONDAY
//        Log.i("it.WEEK_OF_YEAR", it.get(Calendar.WEEK_OF_YEAR).toString())
//        Log.i("firstWeek.WEEK_OF_YEAR", firstWeek.get(Calendar.WEEK_OF_YEAR).toString())
        it.get(Calendar.WEEK_OF_YEAR) - firstWeek.get(Calendar.WEEK_OF_YEAR) + 1
    }
}