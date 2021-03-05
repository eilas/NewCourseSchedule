package com.eilas.newcourseschedule.ui.view

import android.content.DialogInterface
import android.graphics.RectF
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.WeekViewUtil
import com.eilas.newcourseschedule.data.model.CourseInfo
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.data.saveCourse
import com.eilas.newcourseschedule.databinding.AlertAddCourseBinding
import com.eilas.newcourseschedule.databinding.WeekFragmentBinding
import com.eilas.newcourseschedule.ui.schedule.CourseScheduleActivity
import java.util.*

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

        weekFragmentBinding.weekView.apply {
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
                Log.i("time", time.toString())
            }
            maxDate = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                setWeekDate(weekYear, get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY)

                Log.i("time", time.toString())
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

                }

            }
            dropListener = object : WeekView.DropListener {
                override fun onDrop(view: View, date: Calendar) {

                }

            }

            addEventClickListener = object : WeekView.AddEventClickListener {
                override fun onAddEventClicked(startTime: Calendar, endTime: Calendar) {
                    alertAddCourseBinding.root.parent?.apply {
                        (this as ViewGroup).removeAllViews()
                    }
//                    添加课程
                    AlertDialog.Builder(activity!!)
                        .setTitle("添加课程")
                        .setView(alertAddCourseBinding.root)
                        .setPositiveButton(
                            "是",
                            DialogInterface.OnClickListener { dialog, which ->
                                val courseScheduleActivity = context as CourseScheduleActivity
                                val itemStrEndTime = courseScheduleActivity.itemStrEndTime
                                val strTime =
                                    alertAddCourseBinding.strTime.text.toString().toInt() - 1
                                val endTIme =
                                    strTime + alertAddCourseBinding.lastTime.text.toString()
                                        .toInt() - 1

                                saveCourse(
                                    courseScheduleActivity.user, CourseInfo(
                                        courseName = alertAddCourseBinding.courseName.text.toString(),
                                        courseStrTime1 = itemStrEndTime["strTime$strTime"]!!.apply {
                                            set(
                                                startTime.get(Calendar.YEAR),
                                                startTime.get(Calendar.MONTH),
                                                startTime.get(Calendar.DATE)
                                            )
                                        }.time,
                                        courseEndTime1 = itemStrEndTime["endTime$endTIme"]!!.apply {
                                            set(
                                                startTime.get(Calendar.YEAR),
                                                startTime.get(Calendar.MONTH),
                                                startTime.get(Calendar.DATE)
                                            )
                                        }.time,
                                        strWeek = alertAddCourseBinding.strWeek.text.toString()
                                            .toInt(),
                                        lastWeek = alertAddCourseBinding.lastWeek.text.toString()
                                            .toInt(),
                                        info = alertAddCourseBinding.courseInfo.text.toString(),
                                        location = alertAddCourseBinding.location.text.toString()
                                    )
                                )
                            }
                        )
                        .setNegativeButton("否", null)
                        .show()

                }

            }
            scrollListener = object : WeekView.ScrollListener {
                override fun onFirstVisibleDayChanged(
                    newFirstVisibleDay: Calendar,
                    oldFirstVisibleDay: Calendar?
                ) {

                }

            }
        }


//        weekFragmentBinding.gridView.adapter = ScheduleAdapter(activity!!, courseItemList)
    }

    fun changeViewTo(viewName: String) {
        weekFragmentBinding.weekView.numberOfVisibleDays =
            if (viewName.equals("week_view"))
                7
            else
                1
    }
}