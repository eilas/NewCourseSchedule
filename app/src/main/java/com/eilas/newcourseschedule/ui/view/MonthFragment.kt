package com.eilas.newcourseschedule.ui.view

import android.graphics.RectF
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.WeekViewUtil
import com.eilas.newcourseschedule.data.model.CourseItemIndex
import com.eilas.newcourseschedule.databinding.MonthFragmentBinding
import java.time.DayOfWeek
import java.util.*

class MonthFragment : Fragment() {

    private lateinit var monthFragmentBinding: MonthFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        monthFragmentBinding = MonthFragmentBinding.inflate(layoutInflater)
        return monthFragmentBinding.root
    }

    fun initView(courseItemList: ArrayList<CourseItemIndex>) {
        monthFragmentBinding.monthView.apply {
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
                Log.i("date", this.time.toString())
                Log.i("time", this.weekYear.toString() + " " + get(Calendar.WEEK_OF_YEAR))
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    setWeekDate(weekYear, get(Calendar.WEEK_OF_YEAR), DayOfWeek.MONDAY.value)
                }
            }
            maxDate = Calendar.getInstance().apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    setWeekDate(weekYear, get(Calendar.WEEK_OF_YEAR), DayOfWeek.SUNDAY.value)
                }
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
            eventLongPressListener = object : WeekView.EventLongPressListener {
                override fun onEventLongPress(event: WeekViewEvent, eventRect: RectF) {

                }

            }
            emptyViewLongPressListener = object : WeekView.EmptyViewLongPressListener {
                override fun onEmptyViewLongPress(time: Calendar) {

                }

            }
            addEventClickListener = object : WeekView.AddEventClickListener {
                override fun onAddEventClicked(startTime: Calendar, endTime: Calendar) {

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
    }

}

