package com.eilas.newcourseschedule.ui.view

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.data.saveCourse
import com.eilas.newcourseschedule.databinding.AlertAddCourseBinding
import com.eilas.newcourseschedule.databinding.WeekFragmentBinding
import com.eilas.newcourseschedule.ui.schedule.CourseScheduleActivity
import com.eilas.newcourseschedule.ui.view.adapter.ScheduleAdapter
import java.util.*
import kotlin.collections.ArrayList

class WeekFragment : Fragment() {

    private lateinit var weekFragmentBinding: WeekFragmentBinding
    private lateinit var alertAddCourseBinding: AlertAddCourseBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        weekFragmentBinding = WeekFragmentBinding.inflate(layoutInflater)
        alertAddCourseBinding = AlertAddCourseBinding.inflate(layoutInflater)

        return weekFragmentBinding.root
    }

    fun initView(courseItemList: ArrayList<CourseItemIndex>) {
        weekFragmentBinding.gridView.adapter = ScheduleAdapter(activity!!, courseItemList)
        weekFragmentBinding.gridView.setOnItemClickListener { parent, view, position, id ->
            courseItemList[position].let {
                kotlin.runCatching {
                    if (it.isEmpty()) {
                        alertAddCourseBinding.root.parent?.apply {
                            this as ViewGroup
                            removeAllViews()
                        }
//                        添加课程
                        AlertDialog.Builder(activity!!)
                            .setTitle("添加课程")
                            .setView(alertAddCourseBinding.root)
                            .setPositiveButton(
                                "是",
                                DialogInterface.OnClickListener { dialog, which ->
                                    saveCourse((context as CourseScheduleActivity).user, CourseInfo(
                                        courseName = alertAddCourseBinding.courseName.text.toString(),
                                        // TODO: 2021/2/16 两个时间有问题
                                        courseStrTime1 = Date(),
//                                        inflate.findViewById<EditText>(R.id.lastTime).text.toString(),
                                        courseEndTime1 = Date(),
                                        lastWeek = alertAddCourseBinding.lastWeek.text.toString()
                                            .toInt(),
                                        info = alertAddCourseBinding.courseInfo.text.toString(),
                                        courseItemIndexList = ArrayList<CourseItemIndex>().apply {
                                            for (i in 0 until alertAddCourseBinding.lastTime.text.toString()
                                                .toInt())
//                                                gridView一列7个，故加入同一列的循环个courseItem
                                                add(courseItemList[position + i * 7])
                                        }
                                    ))
                                }
                            )
                            .setNegativeButton("否", null)
                            .show()

                    } else {
                        TODO("查看课程信息")
                    }
                }.onFailure {
                    Log.i("failure", it.message)
                }
            }
        }
    }
}