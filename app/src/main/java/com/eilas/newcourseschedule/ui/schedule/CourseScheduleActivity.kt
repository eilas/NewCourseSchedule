package com.eilas.newcourseschedule.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.getAllCourse
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.ui.login.LoginActivity
import com.eilas.newcourseschedule.ui.login.deleteUser
import com.eilas.newcourseschedule.ui.view.adapter.ScheduleAdapter
import kotlinx.android.synthetic.main.activity_course_schedule.*

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
        val courseList = initData()

//        顶栏
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            // TODO: 2021/2/13 设置顶栏图标
        }

//        主界面
        gridView.adapter = ScheduleAdapter(this, courseList)
        gridView.numColumns = 8
        gridView.setOnItemClickListener { parent, view, position, id ->
            courseList[position].let {
//                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
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

//        导航栏
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> {
                    deleteUser(this)
                    startActivity(
                        Intent(
                            this,
                            LoginActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
            true
        }
    }

    fun initData(): ArrayList<CourseInfo> =
        intent.extras?.getParcelable<LoggedInUser>("user")?.let { getAllCourse(it) }!!
}