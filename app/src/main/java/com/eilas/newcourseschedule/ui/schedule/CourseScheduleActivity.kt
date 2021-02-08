package com.eilas.newcourseschedule.ui.schedule

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.ui.view.adapter.ScheduleAdapter
import kotlinx.android.synthetic.main.activity_course_schedule.*

class CourseScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_schedule)


        val courseList = ArrayList<CourseInfo>()
        for (item in 0 until 13) {
            for (day in 0 until 8) {
                courseList.add(CourseInfo("test", item.toString(), day.toString()))
            }
        }

        view_grid.adapter = ScheduleAdapter(this, courseList)
        view_grid.numColumns = 8

        view_grid.setOnItemClickListener { parent, view, position, id ->
            courseList[position].let {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                kotlin.runCatching {
                    if (it.isEmpty) {
                        TODO("添加课程")
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