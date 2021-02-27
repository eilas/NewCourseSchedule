package com.eilas.newcourseschedule.data

import android.content.Context
import java.util.*

fun saveFirstWeek(context: Context, calendar: Calendar) {
    Thread {
        context.getSharedPreferences("date", Context.MODE_PRIVATE).edit()
            .putInt("year", calendar.get(Calendar.YEAR))
            .putInt("month", calendar.get(Calendar.MONTH))
            .putInt("weekOfMonth", calendar.get(Calendar.WEEK_OF_MONTH)).apply()
    }.start()
}

fun loadFirstWeek(context: Context): Calendar? {
    return context.getSharedPreferences("date", Context.MODE_PRIVATE).let {
        val year = it.getInt("id", -1)
        val month = it.getInt("pwd", -1)
        val weekOfMonth = it.getInt("name", -1)

        if (year != -1 && month != -1 && weekOfMonth != -1)
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.WEEK_OF_MONTH, weekOfMonth)
            }
        else
            null
    }
}