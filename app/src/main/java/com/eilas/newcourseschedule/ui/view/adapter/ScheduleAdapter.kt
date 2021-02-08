package com.eilas.newcourseschedule.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.schedule_item.view.*

class ScheduleAdapter(val context: Context, var courseList: ArrayList<CourseInfo>) : BaseAdapter() {
    val gson = Gson()

    override fun getCount(): Int {
        return courseList.size
    }

    override fun getItem(position: Int): Any {
        return courseList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.schedule_item, null)
            viewHolder = ViewHolder(view.course_name, view.course_str_time, view.course_end_time)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        getItem(position).let {
            it as CourseInfo
            viewHolder.courseName.text = it.courseName
            viewHolder.courseStrTime.text = it.courseStrTime
            viewHolder.courseEndTime.text = it.courseEndTime
        }

        return view
    }

    inner class ViewHolder(
        var courseName: TextView,
        var courseStrTime: TextView,
        var courseEndTime: TextView
    )
}