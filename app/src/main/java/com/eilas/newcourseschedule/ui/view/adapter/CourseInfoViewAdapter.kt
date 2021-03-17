package com.eilas.newcourseschedule.ui.view.adapter

import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eilas.newcourseschedule.R

class CourseInfoViewAdapter(val dataList: List<Pair<String, String>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textView3 = view.findViewById<TextView>(R.id.textView3)
        val textView4 = view.findViewById<TextView>(R.id.textView4)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.alert_course_info_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        (holder as VH).apply {
            (dataList[position]).apply {
                textView3.text = first
                textView4.text = second
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}