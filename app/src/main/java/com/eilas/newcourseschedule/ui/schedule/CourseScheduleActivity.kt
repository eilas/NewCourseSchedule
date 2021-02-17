package com.eilas.newcourseschedule.ui.schedule

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.getAllCourse
import com.eilas.newcourseschedule.data.logout
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.data.saveCourse
import com.eilas.newcourseschedule.databinding.ActivityCourseScheduleBinding
import com.eilas.newcourseschedule.databinding.AlertAddCourseBinding
import com.eilas.newcourseschedule.ui.view.adapter.ScheduleAdapter
import java.util.*
import kotlin.collections.ArrayList

// TODO: 2021/2/12 需要设定第一周 ，关联日期，查询时提交
class CourseScheduleActivity : AppCompatActivity() {

    private lateinit var user: LoggedInUser
    private lateinit var activityCourseScheduleBinding: ActivityCourseScheduleBinding
    private lateinit var alertAddCourseBinding: AlertAddCourseBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCourseScheduleBinding = ActivityCourseScheduleBinding.inflate(layoutInflater)
        alertAddCourseBinding = AlertAddCourseBinding.inflate(layoutInflater)
        setContentView(activityCourseScheduleBinding.root)

        user = intent.extras?.getParcelable<LoggedInUser>("user")!!
        initView()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activityCourseScheduleBinding.drawerLayout.openDrawer(GravityCompat.START)
            R.id.changeView -> TODO("切换视图！")
        }
        return true
    }

    fun initView() {
        val courseItemList = initData()

//        顶栏
        setSupportActionBar(activityCourseScheduleBinding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            // TODO: 2021/2/13 设置顶栏图标
        }

//        主界面
        activityCourseScheduleBinding.gridView.adapter = ScheduleAdapter(this, courseItemList)
        activityCourseScheduleBinding.gridView.setOnItemClickListener { parent, view, position, id ->
            courseItemList[position].let {
                kotlin.runCatching {
                    if (it.isEmpty()) {
                        alertAddCourseBinding.root.parent?.apply {
                            this as ViewGroup
                            removeAllViews()
                        }
//                        添加课程
                        AlertDialog.Builder(this)
                            .setTitle("添加课程")
                            .setView(alertAddCourseBinding.root)
                            .setPositiveButton(
                                "是",
                                DialogInterface.OnClickListener { dialog, which ->
                                    saveCourse(user, CourseInfo(
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

//        导航栏
        activityCourseScheduleBinding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> logout(this)
            }
            true
        }
    }

    fun initData(): ArrayList<CourseItemIndex> {
        return getAllCourse(user)
    }
}