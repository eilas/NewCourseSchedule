package com.eilas.newcourseschedule.ui.schedule

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.*
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.databinding.ActivityCourseScheduleBinding
import com.eilas.newcourseschedule.databinding.AlertCalendarBinding
import com.eilas.newcourseschedule.databinding.AlertCourseCountDayBinding
import com.eilas.newcourseschedule.databinding.AlertCourseTimePickerBinding
import com.eilas.newcourseschedule.ui.view.WeekFragment
import java.util.*
import kotlin.collections.HashMap

// TODO: 2021/2/12 需要设定第一周 ，关联日期，查询时提交
class CourseScheduleActivity : AppCompatActivity() {

    lateinit var user: LoggedInUser
    lateinit var firstWeek: Calendar
    lateinit var itemStrEndTime: Map<String, Calendar>
    private lateinit var activityCourseScheduleBinding: ActivityCourseScheduleBinding
    private lateinit var alertCalendarBinding: AlertCalendarBinding
    private lateinit var alertCourseCountDayBinding: AlertCourseCountDayBinding
    private lateinit var alertCourseTimePickerBinding: AlertCourseTimePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCourseScheduleBinding = ActivityCourseScheduleBinding.inflate(layoutInflater)
        alertCalendarBinding = AlertCalendarBinding.inflate(layoutInflater)
        alertCourseCountDayBinding = AlertCourseCountDayBinding.inflate(layoutInflater)
        alertCourseTimePickerBinding = AlertCourseTimePickerBinding.inflate(layoutInflater)
        setContentView(activityCourseScheduleBinding.root)

        user = intent.extras?.getParcelable<LoggedInUser>("user")!!

        val loadFirstWeek = loadFirstWeek(this)
        if (loadFirstWeek == null)
            Toast.makeText(this, "请设置第一周！", Toast.LENGTH_LONG).show()
        else
            firstWeek = loadFirstWeek

        val loadItemStrEndTime = loadItemStrEndTime(this)
        if (loadItemStrEndTime.isEmpty())
            Toast.makeText(this, "请设置课节数及上课时间！", Toast.LENGTH_LONG).show()
        else
            itemStrEndTime = loadItemStrEndTime

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
//                设置第一周
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
//                设置课节数及上课时间
                R.id.setItemStrEndTime -> {
                    AlertDialog.Builder(this).setTitle("设置课节数及上课时间")
                        .setView(alertCourseCountDayBinding.root.apply {
                            parent?.apply {
                                (this as ViewGroup).removeAllViews()
                            }
                        })
                        .setPositiveButton("是", DialogInterface.OnClickListener { dialog, which ->
                            val count =
                                alertCourseCountDayBinding.courseCountDay.text.toString().toInt()
                            val calendarMap = HashMap<String, Calendar>()

                            fun showAlertDialog(i: Int, total: Int) {
                                if (i < total)
                                    AlertDialog.Builder(this).setTitle("第${i + 1}节，共${total}节")
                                        .setView(alertCourseTimePickerBinding.let {
                                            it.strTimePicker.setIs24HourView(true)
                                            it.endTimePicker.setIs24HourView(true)
                                            it.root.apply {
                                                parent?.apply {
                                                    (this as ViewGroup).removeAllViews()
                                                }
                                            }
                                        })
                                        .setPositiveButton(
                                            "下一节",
                                            DialogInterface.OnClickListener { dialog, which ->
                                                calendarMap.apply {
                                                    put(
                                                        "strTime$i",
                                                        Calendar.getInstance().apply {
                                                            set(
                                                                Calendar.HOUR_OF_DAY,
                                                                alertCourseTimePickerBinding.strTimePicker.hour
                                                            )
                                                            set(
                                                                Calendar.MINUTE,
                                                                alertCourseTimePickerBinding.strTimePicker.minute
                                                            )
                                                        })
                                                    put(
                                                        "endTime$i",
                                                        Calendar.getInstance().apply {
                                                            set(
                                                                Calendar.HOUR_OF_DAY,
                                                                alertCourseTimePickerBinding.endTimePicker.hour
                                                            )
                                                            set(
                                                                Calendar.MINUTE,
                                                                alertCourseTimePickerBinding.endTimePicker.minute
                                                            )
                                                        })
                                                }

                                                showAlertDialog(i + 1, total)
                                            })
                                        .show()
                                else
//                                    saveItemStrEndTime在showAlertDialog里执行能保证所有AlertDialog显示完后再执行
                                    saveItemStrEndTime(this, calendarMap)
                            }

                            var i = 0
                            showAlertDialog(i, count)

                        }).show()
                }
//                登出
                R.id.logout -> logout(this)
            }
            true
        }
    }

    fun initData(): ArrayList<CourseItemIndex> {
        return getAllCourse(user)
    }
}