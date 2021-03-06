package com.eilas.newcourseschedule.ui

import android.content.*
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.*
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.databinding.ActivityCourseScheduleBinding
import com.eilas.newcourseschedule.databinding.AlertCalendarBinding
import com.eilas.newcourseschedule.databinding.AlertCourseCountDayBinding
import com.eilas.newcourseschedule.databinding.AlertCourseTimePickerBinding
import com.eilas.newcourseschedule.service.CourseStartRemindService
import com.eilas.newcourseschedule.ui.view.WeekFragment
import com.eilas.newcourseschedule.ui.view.adapter.BasicDoubleColumnAdapter
import com.eilas.newcourseschedule.ui.view.adapter.BasicTripleColumnAdapter
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

class CourseScheduleActivity : AppCompatActivity() {

    lateinit var user: LoggedInUser
    lateinit var firstWeek: Calendar
    lateinit var itemStrEndTime: Map<String, Calendar>
    lateinit var remindService: CourseStartRemindService.CourseListBinder
    val infoList = ArrayList<Triple<String, String, String>>()
    private lateinit var activityCourseScheduleBinding: ActivityCourseScheduleBinding
    private lateinit var alertCalendarBinding: AlertCalendarBinding
    private lateinit var alertCourseCountDayBinding: AlertCourseCountDayBinding
    private lateinit var alertCourseTimePickerBinding: AlertCourseTimePickerBinding

    private val handler = Handler(WeakReference(Handler.Callback {
        when (it.what) {
            1 -> {
//                init WeekFragment
                val courseList = it.obj as ArrayList<CourseInfo>
                supportFragmentManager.findFragmentById(R.id.weekFragment).apply {
                    (this as WeekFragment).initView(courseList, firstWeek)
                }
//                init service
                initService(courseList)

                Log.i("all course", courseList.toString())
            }
            2 -> {
//                响应推送消息
                val mutableList = it.obj as MutableList<String>
                Toast.makeText(this, mutableList.removeFirst(), Toast.LENGTH_SHORT).show()
                infoList.add(
                    Triple(
                        mutableList.component1(),
                        mutableList.component2(),
                        mutableList.component3()
                    )
                )
            }
        }
        true
    }).get())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCourseScheduleBinding = ActivityCourseScheduleBinding.inflate(layoutInflater)
        alertCalendarBinding = AlertCalendarBinding.inflate(layoutInflater)
        alertCourseCountDayBinding = AlertCourseCountDayBinding.inflate(layoutInflater)
        alertCourseTimePickerBinding = AlertCourseTimePickerBinding.inflate(layoutInflater)
        setContentView(activityCourseScheduleBinding.root)

        user = intent.extras?.getParcelable<LoggedInUser>("user")!!

        initView()
        initMQ(user.id)
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

    private fun initView() {
        initData()

//        顶栏
        setSupportActionBar(activityCourseScheduleBinding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        }

//        导航栏
        activityCourseScheduleBinding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
//                设置第一周
                R.id.setFirstWeek -> setFirstWeek()
//                设置课节数及上课时间
                R.id.setItemStrEndTime -> setItemStrEndTime()
//                信息列表
                R.id.infoList -> {
                    AlertDialog.Builder(this).setTitle("信息列表")
                        .setView(RecyclerView(this).apply {
                            layoutManager = LinearLayoutManager(context).apply {
                                orientation = LinearLayoutManager.VERTICAL
                            }
                            adapter = BasicTripleColumnAdapter(infoList, null)
                            addItemDecoration(
                                DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
                            )
                        }).show()
                }
//                登出
                R.id.logout -> logout(this)

                R.id.getWork -> {
                    AlertDialog.Builder(this).setTitle("查看WorkManager任务")
                        .setView(RecyclerView(this).apply {
                            layoutManager = LinearLayoutManager(context).apply {
                                orientation = LinearLayoutManager.VERTICAL
                            }
                            adapter =
                                BasicDoubleColumnAdapter(ArrayList<Pair<String, String>>().apply {
                                    val instance =
                                        WorkManager.getInstance(this@CourseScheduleActivity)
                                    add("看门狗" to instance.getWorkInfosByTagLiveData("看门狗").value.toString())
                                    add("课前提醒" to instance.getWorkInfosByTagLiveData("课前提醒").value.toString())
                                }, null)
                        }).show()
                }
            }
            true
        }
    }

    private fun initData() {
        kotlin.runCatching {
//            init firstWeek and the str-end time for each item
            firstWeek = loadFirstWeek(this)!!
            Log.i("第一周", firstWeek.time.toString())
            itemStrEndTime = loadItemStrEndTime(this)
            if (itemStrEndTime.isEmpty()) throw Exception("设置课节数及上课时间")
            refreshData(handler)
        }.onFailure {
            if (it.message.equals("设置课节数及上课时间")) {
                Toast.makeText(this, "请设置课节数及上课时间！", Toast.LENGTH_LONG).show()
                setItemStrEndTime(true)
            } else {
                Toast.makeText(this, "请设置第一周！", Toast.LENGTH_LONG).show()
                setFirstWeek()
            }
//            firstWeek = loadFirstWeek(this)!!

//            return initData()
        }
    }

    fun refreshData(handler: Handler) {
        getAllCourse(user, getThisWeek(firstWeek), handler)
    }

    private fun setFirstWeek() {
        var alertDialog: AlertDialog? = null
        alertDialog = AlertDialog.Builder(this).setTitle("设置第一周")
            .setView(CalendarView(this).apply {
                firstDayOfWeek = Calendar.MONDAY
                setOnDateChangeListener { view, year, month, dayOfMonth ->
                    saveFirstWeek(this@CourseScheduleActivity,
                        Calendar.getInstance().apply {
                            firstDayOfWeek = Calendar.MONDAY
                            set(year, month, dayOfMonth)
                            firstWeek = this
                        })
                    alertDialog?.dismiss()
//                    设置first week后需要initData，主要是为了调用handler的callback方法
                    initData()
                }
            }).show()
    }

    private fun setItemStrEndTime(needRestart: Boolean = false) {
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
                                        put("strTime$i", Calendar.getInstance().apply {
                                            set(
                                                Calendar.HOUR_OF_DAY,
                                                alertCourseTimePickerBinding.strTimePicker.hour
                                            )
                                            set(
                                                Calendar.MINUTE,
                                                alertCourseTimePickerBinding.strTimePicker.minute
                                            )
                                        })
                                        put("endTime$i", Calendar.getInstance().apply {
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
                    else {
//                        saveItemStrEndTime在showAlertDialog里执行能保证所有AlertDialog显示完后再执行
                        saveItemStrEndTime(this, calendarMap)
                        itemStrEndTime = calendarMap

//                        第一次使用APP时需要设置每节课时间，设置完后需要重新启动activity
                        if (needRestart) {
                            startActivity(
                                Intent(this, CourseScheduleActivity::class.java).setFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                ).putExtras(Bundle().apply {
                                    putParcelable(
                                        "user",
                                        user as Parcelable
                                    )
                                })
                            )
                        }
                    }
                }

                var i = 0
                showAlertDialog(i, count)

            }).show()
    }

    fun initService(courseList: List<CourseInfo>) {
        Intent(this, CourseStartRemindService::class.java).let {
            bindService(it, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    (service as CourseStartRemindService.CourseListBinder).apply {
                        if (this.courseList == null) this.courseList = courseList
                        this.dayStartTime = Calendar.getInstance().apply {
                            itemStrEndTime["strTime0"]!!.let {
                                set(Calendar.HOUR_OF_DAY, it[Calendar.HOUR_OF_DAY])
                                set(Calendar.MINUTE, it[Calendar.MINUTE])
                            }
                        }
                        this.dayEndTime = Calendar.getInstance().apply {
                            itemStrEndTime["endTime${itemStrEndTime.size / 2 - 1}"]!!.let {
                                set(Calendar.HOUR_OF_DAY, it[Calendar.HOUR_OF_DAY])
                                set(Calendar.MINUTE, it[Calendar.MINUTE])
                            }
                        }
//                        bind service成功后start service
                        startService(it)

                        remindService = this
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    Log.i("CourseScheduleActivity", "service disconnected")
//                    stopService(it)
                }
            }, Context.BIND_AUTO_CREATE)
        }

    }

    fun initMQ(userId: String) {
        MQHelpers.apply {
            init(userId)
            connect("notify/$userId", handler)
        }
    }
}