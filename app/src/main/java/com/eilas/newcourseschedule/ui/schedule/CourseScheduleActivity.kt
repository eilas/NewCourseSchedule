package com.eilas.newcourseschedule.ui.schedule

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.getAllCourse
import com.eilas.newcourseschedule.data.loadFirstWeek
import com.eilas.newcourseschedule.data.logout
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.data.saveFirstWeek
import com.eilas.newcourseschedule.databinding.ActivityCourseScheduleBinding
import com.eilas.newcourseschedule.databinding.AlertCalendarBinding
import com.eilas.newcourseschedule.ui.view.WeekFragment
import java.util.*

// TODO: 2021/2/12 需要设定第一周 ，关联日期，查询时提交
class CourseScheduleActivity : AppCompatActivity() {

    lateinit var user: LoggedInUser
    lateinit var firstWeek: Calendar
    private lateinit var activityCourseScheduleBinding: ActivityCourseScheduleBinding
    private lateinit var alertCalendarBinding: AlertCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCourseScheduleBinding = ActivityCourseScheduleBinding.inflate(layoutInflater)
        alertCalendarBinding = AlertCalendarBinding.inflate(layoutInflater)
        setContentView(activityCourseScheduleBinding.root)

        user = intent.extras?.getParcelable<LoggedInUser>("user")!!
        val loadFirstWeek = loadFirstWeek(this)
        if (loadFirstWeek == null)
            Toast.makeText(this, "请设置第一周！", Toast.LENGTH_LONG).show()
        else
            firstWeek = loadFirstWeek

        initView()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activityCourseScheduleBinding.drawerLayout.openDrawer(GravityCompat.START)
            R.id.changeView -> {
                activityCourseScheduleBinding.toolbar.menu.findItem(R.id.changeView).apply {
                    val weekFragment =
                        supportFragmentManager.findFragmentById(R.id.weekFragment) as WeekFragment
                    if (title.equals("week_view")) {
                        Log.i("menu", "切换成日视图")
                        setIcon(android.R.drawable.ic_menu_month)
                        title = "day_view"
                    } else {
                        Log.i("menu", "切换成周视图")
                        setIcon(android.R.drawable.ic_menu_week)
                        title = "week_view"
                    }
                    weekFragment.changeViewTo(title as String)
                }
            }
        }
        return true
    }

    fun initView() {
        val courseItemList = initData()

//        顶栏
        setSupportActionBar(activityCourseScheduleBinding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        }

//        主界面
        supportFragmentManager.findFragmentById(R.id.weekFragment).apply {
            this as WeekFragment
            this.initView(courseItemList)
        }
/*
        supportFragmentManager.findFragmentById(R.id.monthFragment).apply {
            this as MonthFragment
            this.initView(courseItemList)

        }
*/

//        导航栏
        activityCourseScheduleBinding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.setFirstWeek -> {
                    var alertDialog: AlertDialog? = null
                    alertDialog = AlertDialog.Builder(this).setTitle("设置第一周")
                        .setView(CalendarView(this).apply {
                            firstDayOfWeek = Calendar.MONDAY
                            setOnDateChangeListener { view, year, month, dayOfMonth ->
                                saveFirstWeek(
                                    this@CourseScheduleActivity,
                                    Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    })
                                alertDialog?.dismiss()
                            }
                        }).show()
                }
                R.id.setItemStrEndTime -> {
                }
                R.id.logout -> logout(this)
            }
            true
        }
    }

    fun initData(): ArrayList<CourseItemIndex> {
        return getAllCourse(user)
    }
}