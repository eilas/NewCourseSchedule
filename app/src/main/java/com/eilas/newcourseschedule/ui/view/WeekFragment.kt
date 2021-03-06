package com.eilas.newcourseschedule.ui.view

import android.content.Intent
import android.graphics.RectF
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alamkanak.weekview.*
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.*
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.databinding.AlertAddCourseBinding
import com.eilas.newcourseschedule.databinding.WeekFragmentBinding
import com.eilas.newcourseschedule.ui.CommunicationActivity
import com.eilas.newcourseschedule.ui.CourseScheduleActivity
import com.eilas.newcourseschedule.ui.view.adapter.BasicDoubleColumnAdapter
import com.eilas.newcourseschedule.ui.view.adapter.BasicTripleColumnAdapter
import com.eilas.newcourseschedule.ui.view.adapter.CourseItemColorAdapter
import com.eilas.newcourseschedule.widget.DesktopWidgetProvider
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class WeekFragment : Fragment() {

    private lateinit var weekFragmentBinding: WeekFragmentBinding
    private lateinit var alertAddCourseBinding: AlertAddCourseBinding
    private lateinit var courseList: ArrayList<CourseInfo>
    val weekViewEventList: ArrayList<WeekViewEvent> = ArrayList()
    val singleCourseInfoCache = LruCache<String, ArrayList<Pair<String, String>>>(8)

    var tempAdapter: RecyclerView.Adapter<*>? = null
    private val handler: Handler = Handler(WeakReference(Handler.Callback {
        when (it.what) {
            1 -> {
                courseList.apply {
                    clear()
                    (it.obj as ArrayList<CourseInfo>).forEach { add(it) }
                }
//                重新展示全部课程
                weekFragmentBinding.weekView.notifyDataSetChanged()
//                通知CourseStartRemindService课程改变
                ((context as CourseScheduleActivity).supportFragmentManager.findFragmentById(R.id.weekFragment) as WeekFragment).handler.sendMessage(
                    Message.obtain().apply { what = 99 })
            }
            2 -> {
//                重新获取全部课程数据
                (context as CourseScheduleActivity).apply {
                    refreshData((supportFragmentManager.findFragmentById(R.id.weekFragment) as WeekFragment).handler)
                }
            }
            3 -> {
//                展示课程数据
                val pairList = it.obj as ArrayList<Pair<String, String>>
                Log.i("pair list", pairList.toString())
//                pairList.removeFirst().second获得course id
                singleCourseInfoCache[pairList.removeFirst().second].apply {
                    clear()
                    pairList.forEach { add(it) }
                }
                tempAdapter?.notifyDataSetChanged()
            }
            4 -> {
//                展示课程同学数据
                val classmateList = it.obj as ArrayList<Triple<String, String, String>>
                Log.i("classmate list", classmateList.toString())

                (tempAdapter as BasicTripleColumnAdapter).apply {
                    (dataList as ArrayList).apply {
                        clear()
                        classmateList.forEach { add(it) }
                    }
                    notifyDataSetChanged()
                }
            }
            5 -> {
//                删除课程后，通知CourseStartRemindService课程改变
                ((context as CourseScheduleActivity).supportFragmentManager.findFragmentById(R.id.weekFragment) as WeekFragment).handler.sendMessage(
                    Message.obtain().apply { what = 99 })
            }
            99 -> {
//                更新CourseStartRemindService数据
                (activity as CourseScheduleActivity).remindService.notifyDataListChanged()
//                更新ListRemoteViewsService数据（如果有的话）
                context?.sendBroadcast(
                    Intent().setPackage(context!!.packageName)
                        .setAction(DesktopWidgetProvider.ACTION_DATASET_UPDATE)
                )
            }
        }
        true
    }).get())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        weekFragmentBinding = WeekFragmentBinding.inflate(layoutInflater)
        alertAddCourseBinding = AlertAddCourseBinding.inflate(layoutInflater)

        return weekFragmentBinding.root
    }

    fun initView(courseItemList: ArrayList<CourseInfo>, firstWeek: Calendar) {
        courseList = courseItemList
//        绑定颜色数据
        val courseItemColorAdapter =
            CourseItemColorAdapter(this@WeekFragment.context!!, weekViewEventList)
        courseItemColorAdapter.bindColor()

        weekFragmentBinding.weekView.apply {
//            由于多线程（可能）原因，未有课程数据时便加载完成，需要重新加载
            notifyDataSetChanged()
//            表项样式
            numberOfVisibleDays = 7
            columnGap =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics)
                    .toInt()
            textSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics)
            eventTextSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics)

//            header行样式
            dateTimeInterpreter = object : DateTimeInterpreter {
                override fun getFormattedWeekDayTitle(date: Calendar): String {
                    return WeekViewUtil.getWeekdayWithNumericDayAndMonthFormat(context, true)
                        .format(date.time)
                }

                override fun getFormattedTimeOfDay(hour: Int, minutes: Int): String {
                    val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
                        ?: SimpleDateFormat("HH:mm", Locale.getDefault())
                    return timeFormat.format(Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minutes)
                    }.time)
                }

            }

//            通过minDate和maxDate固定当前周————共七天
            minDate = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                setWeekDate(weekYear, get(Calendar.WEEK_OF_YEAR), Calendar.MONDAY)
            }
            maxDate = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                setWeekDate(weekYear, get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY)
            }

//            设置开始结束时间
            setLimitTime(8, 21)

//            listener
            emptyViewClickListener = object : WeekView.EmptyViewClickListener {
                override fun onEmptyViewClicked(date: Calendar) {
                    Toast.makeText(context, date.time.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            eventClickListener = object : WeekView.EventClickListener {
                override fun onEventClick(event: WeekViewEvent, eventRect: RectF) {
                    AlertDialog.Builder(context).setTitle("课程详情")
                        .setView(RecyclerView(context).apply {
                            layoutManager = LinearLayoutManager(context).apply {
                                orientation = LinearLayoutManager.VERTICAL
                            }

                            adapter = BasicDoubleColumnAdapter(singleCourseInfoCache.let {
                                try {
                                    return@let it[event.id] ?: throw Exception()
                                } catch (e: Exception) {
                                    it.put(event.id, ArrayList<Pair<String, String>>())
                                    getSingleCourse(
                                        (context as CourseScheduleActivity).user,
                                        event.id!!,
                                        this@WeekFragment.handler
                                    )
                                    return@let it[event.id]
                                }
                            }, null)

                            tempAdapter = adapter

                            addItemDecoration(
                                DividerItemDecoration(
                                    this.context,
                                    DividerItemDecoration.VERTICAL
                                )
                            )
                        }).setNegativeButton("查看同学") { dialog, which ->
                            AlertDialog.Builder(context).setTitle("课程同学")
                                .setView(RecyclerView(context).apply {
                                    layoutManager = LinearLayoutManager(context).apply {
                                        orientation = LinearLayoutManager.VERTICAL
                                    }
                                    adapter =
                                        BasicTripleColumnAdapter(ArrayList()) { toUserId, time ->
                                            (context as CourseScheduleActivity).user.apply {
                                                sendNotifyToClassmate(
                                                    id,
                                                    toUserId,
                                                    name,
                                                    time,
                                                    singleCourseInfoCache[event.id].first().second,
                                                    this@WeekFragment.handler
                                                )
                                            }
                                            true
                                        }
                                    tempAdapter = adapter
                                    addItemDecoration(
                                        DividerItemDecoration(
                                            this.context,
                                            DividerItemDecoration.VERTICAL
                                        )
                                    )
                                    getClassmate(
                                        (context as CourseScheduleActivity).user,
                                        event.id.toString(),
                                        this@WeekFragment.handler
                                    )
                                }).show()
                        }.setPositiveButton("互动（大嘘）") { dialog, which ->
                            val (userId, _, userName, _) = (context as CourseScheduleActivity).user
                            startActivity(
                                Intent(context, CommunicationActivity::class.java)
                                    .putExtra("courseId", event.id)
                                    .putExtra(
                                        "courseName",
                                        singleCourseInfoCache[event.id][0].component2()
                                    )
                                    .putExtra("userId", userId)
                                    .putExtra("userName", userName)
                            )
                        }.show()
                }
            }

            eventLongPressListener = object : WeekView.EventLongPressListener {
                override fun onEventLongPress(event: WeekViewEvent, eventRect: RectF) {
//                    长按删除
                    var courseInfo: CourseInfo? = null
                    var revoke: Boolean = false
                    if (courseList.removeIf { course ->
                            course.id.equals(event.id).also { if (it) courseInfo = course }
                        }) {
                        notifyDataSetChanged()
                        Snackbar.make(this@apply, "课程已删除", Snackbar.LENGTH_LONG).setAction("撤销") {
                            courseList.add(courseInfo!!)
                            revoke = true
                            notifyDataSetChanged()
                        }.addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if (!revoke) dropCourse(
                                    (context as CourseScheduleActivity).user,
                                    courseInfo!!,
                                    this@WeekFragment.handler
                                )
                            }
                        }).show()
                    }
                }

            }

            dropListener = object : WeekView.DropListener {
                override fun onDrop(view: View, date: Calendar) {
                    Toast.makeText(this@WeekFragment.context, "drop", Toast.LENGTH_SHORT).show()
                }
            }

            addEventClickListener = object : WeekView.AddEventClickListener {
                override fun onAddEventClicked(startTime: Calendar, endTime: Calendar) {
                    alertAddCourseBinding.root.parent?.apply {
                        (this as ViewGroup).removeAllViews()
                    }
//                    添加课程
                    AlertDialog.Builder(context)
                        .setTitle("添加课程")
                        .setView(alertAddCourseBinding.root)
                        .setPositiveButton("是") { dialog, which ->
                            val courseScheduleActivity = context as CourseScheduleActivity
                            val itemStrEndTime = courseScheduleActivity.itemStrEndTime
                            val strTime =
                                alertAddCourseBinding.strTime.editText!!.text.toString().toInt() - 1
                            val endTIme =
                                strTime + alertAddCourseBinding.lastTime.editText!!.text.toString()
                                    .toInt() - 1
                            val strWeek = alertAddCourseBinding.strWeek.editText!!.text.toString()
                                .toInt()
                            val lastWeek = alertAddCourseBinding.lastWeek.editText!!.text.toString()
                                .toInt()
                            saveCourse(
                                courseScheduleActivity.user, CourseInfo(
                                    courseName = alertAddCourseBinding.courseName.editText!!.text.toString(),
//                                        课程时间设置为课程第一周的具体时间
                                    courseStrTime1 = itemStrEndTime["strTime$strTime"]!!.let {
//                                            将itemStrEndTime的时分与 通过firstWeek和strWeek获取的课程第一周 和startTime的周几拼接，courseEndTime1下同
                                        (firstWeek.clone() as Calendar).apply {
                                            add(Calendar.DATE, (strWeek - 1) * 7)//第几周
                                            set(
                                                Calendar.DAY_OF_WEEK,
                                                startTime.get(Calendar.DAY_OF_WEEK)
                                            )//周几
                                            set(
                                                Calendar.HOUR_OF_DAY,
                                                it[Calendar.HOUR_OF_DAY]
                                            )//时
                                            set(Calendar.MINUTE, it[Calendar.MINUTE])//分
                                        }
                                    }.time,
                                    courseEndTime1 = itemStrEndTime["endTime$endTIme"]!!.let {
                                        (firstWeek.clone() as Calendar).apply {
                                            add(Calendar.DATE, (strWeek - 1) * 7)
                                            set(
                                                Calendar.DAY_OF_WEEK,
                                                startTime.get(Calendar.DAY_OF_WEEK)
                                            )
                                            set(Calendar.HOUR_OF_DAY, it[Calendar.HOUR_OF_DAY])
                                            set(Calendar.MINUTE, it[Calendar.MINUTE])
                                        }
                                    }.time,
                                    strWeek = strWeek,
                                    lastWeek = lastWeek,
                                    info = alertAddCourseBinding.courseInfo.editText!!.text.toString(),
                                    location = alertAddCourseBinding.location.editText!!.text.toString()
                                ), this@WeekFragment.handler
                            )
                        }
                        .setNegativeButton("否", null)
                        .show()
                        .getButton(AlertDialog.BUTTON_POSITIVE).let {
                            TextCheck(
                                alertAddCourseBinding.courseName,
                                alertAddCourseBinding.strTime,
                                alertAddCourseBinding.lastTime,
                                alertAddCourseBinding.strWeek,
                                alertAddCourseBinding.lastWeek,
                            ).apply {
                                textIsNotEmpty(
                                    alertAddCourseBinding.courseName,
                                    alertAddCourseBinding.strTime,
                                    alertAddCourseBinding.lastTime,
                                    alertAddCourseBinding.strWeek,
                                    alertAddCourseBinding.lastWeek,
                                    unclickedView = it
                                )
                                textIsNumber(
                                    alertAddCourseBinding.strTime,
                                    alertAddCourseBinding.lastTime,
                                    alertAddCourseBinding.strWeek,
                                    alertAddCourseBinding.lastWeek,
                                    unclickedView = it
                                )
                            }
                        }
                }
            }

            scrollListener = object : WeekView.ScrollListener {
                override fun onFirstVisibleDayChanged(
                    newFirstVisibleDay: Calendar,
                    oldFirstVisibleDay: Calendar?
                ) {

                }

            }

            monthChangeListener = object : MonthLoader.MonthChangeListener {
                /*
                * This method is called three times: once to load the previous month,
                *  once to load the next month and once to load the current month.
                * That's why you can have three times the same event at the same place if you mess up with the configuration.
                * */
                override fun onMonthChange(
                    newYear: Int,
                    newMonth: Int
                ): MutableList<out WeekViewEvent>? {
//                    Log.i("monthChangeListener", "month change to $newYear $newMonth")
//                    通过判断时间在加载上月和下月时返回null
                    val today = Calendar.getInstance().apply {
                        set(Calendar.YEAR, newYear)
                        set(Calendar.MONTH, newMonth - 1)
                    }
                    if (today.time.before(minDate?.time) || today.time.after(maxDate?.let {
                            it.set(Calendar.HOUR_OF_DAY, 23)
                            it.set(Calendar.MINUTE, 59)
                            it.set(Calendar.SECOND, 59)
                            it.time
                        })) {
                        return null
                    }

                    return weekViewEventList.apply {
                        clear()
                        courseList.forEach {
                            add(
                                WeekViewEvent(it.id,
                                    it.courseName,
                                    it.location,
                                    Calendar.getInstance().apply { time = it.courseStrTime1 },
                                    Calendar.getInstance()
                                        .apply { time = it.courseEndTime1 })
                            )
                            if (it.courseStrTime2 != null && it.courseEndTime2 != null) {
                                add(
                                    WeekViewEvent(it.id,
                                        it.courseName,
                                        it.location,
                                        Calendar.getInstance().apply { time = it.courseStrTime2 },
                                        Calendar.getInstance()
                                            .apply { time = it.courseEndTime2 })
                                )
                            }
                        }
//                        刷新颜色数据
                        courseItemColorAdapter.refresh()

                    }
                }
            }
        }
    }

    // TODO: 2021/3/14 7/3视图，设置icon
    fun changeViewTo(viewName: String) {
        weekFragmentBinding.weekView.numberOfVisibleDays =
            if (viewName.equals("week_view"))
                7
            else
                1
    }
}