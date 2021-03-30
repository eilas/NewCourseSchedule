package com.eilas.newcourseschedule.ui.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eilas.newcourseschedule.R

class BasicTripleColumnAdapter(val dataList: List<Triple<String, String, String>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textView5 = view.findViewById<TextView>(R.id.textView5)
        val textView6 = view.findViewById<TextView>(R.id.textView6)
        val textView7 = view.findViewById<TextView>(R.id.textView7)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.triple_column_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        (holder as VH).apply {
            (dataList[position]).apply {
                textView5.text = component1()
                textView6.text = component2()
                textView7.text = component3()
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}
