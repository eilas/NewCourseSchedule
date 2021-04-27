package com.eilas.newcourseschedule.ui.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eilas.newcourseschedule.R

class BasicDoubleColumnAdapter(
    val dataList: List<Pair<String, String>>,
    val longClickMethod: (() -> Boolean)?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textView3 = view.findViewById<TextView>(R.id.textView3)
        val textView4 = view.findViewById<TextView>(R.id.textView4)

        init {
            if (longClickMethod != null) {
                view.setOnLongClickListener {
                    longClickMethod.invoke()
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.double_column_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        (holder as VH).apply {
            (dataList[position]).apply {
                textView3.text = component1()
                textView4.text = component2()
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}