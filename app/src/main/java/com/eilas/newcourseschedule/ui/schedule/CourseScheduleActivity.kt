package com.eilas.newcourseschedule.ui.schedule

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.getAllCourse
import com.eilas.newcourseschedule.data.logout
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.ui.view.adapter.ScheduleAdapter
import kotlinx.android.synthetic.main.activity_course_schedule.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: 2021/2/12 需要设定第一周 ，关联日期，查询时提交
class CourseScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_schedule)

        initView()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

    fun initView() {
        val courseItemList = initData()

//        顶栏
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            // TODO: 2021/2/13 设置顶栏图标
        }

//        主界面
        gridView.adapter = ScheduleAdapter(this, courseItemList)
        gridView.numColumns = 8
        gridView.setOnItemClickListener { parent, view, position, id ->
            courseItemList[position].let {
//                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                kotlin.runCatching {
                    if (it.isEmpty()) {
                        TODO("添加课程")
                        val inflate = View.inflate(this, R.layout.alert_add_course, null)
                        AlertDialog.Builder(this)
                            .setTitle("添加课程")
                            .setView(inflate)
                            .setPositiveButton(
                                "是",
                                DialogInterface.OnClickListener { dialog, which ->
                                    CourseInfo(
                                        courseName = inflate.findViewById<EditText>(R.id.courseName).text.toString(),
                                        courseStrTime1 = Date(),
//                                        inflate.findViewById<EditText>(R.id.lastTime).text.toString(),
                                        courseEndTime1 = Date(),
                                        lastWeek = inflate.findViewById<EditText>(R.id.lastWeek).text.toString()
                                            .toInt(),
                                        info = inflate.findViewById<EditText>(R.id.courseInfo).text.toString(),
                                        courseItemIndexList = ArrayList<CourseItemIndex>().apply {
                                            for (i in 0 until inflate.findViewById<EditText>(R.id.lastTime).text.toString().toInt())
//                                                gridView一列8个，故加入同一列的循环个courseItem
                                                add(courseItemList[position + i * 8])
                                        }
                                    )
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

//        导航栏
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> logout(this)
            }
            true
        }
    }

    fun initData(): ArrayList<CourseItemIndex> =
        intent.extras?.getParcelable<LoggedInUser>("user")?.let { getAllCourse(it) }!!
}