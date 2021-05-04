package com.eilas.newcourseschedule.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.getDayCourse
import com.eilas.newcourseschedule.data.getThisWeek
import com.eilas.newcourseschedule.data.loadFirstWeek
import com.eilas.newcourseschedule.data.loadUser
import com.eilas.newcourseschedule.data.model.CourseInfo
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ListRemoteViewsService : RemoteViewsService() {
    interface ListRemoteViewsFactory : RemoteViewsFactory {
        fun initData()
        fun getActiveItem(): String
        fun refresh()
    }

    companion object {
        lateinit var listRemoteViewsFactory: ListRemoteViewsFactory
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        listRemoteViewsFactory = object : ListRemoteViewsFactory {
            val todayCourseInfoList = ArrayList<CourseInfo>()
            private val handler = Handler(WeakReference(Handler.Callback {
                when (it.what) {
                    1 -> {
                        todayCourseInfoList.apply {
                            clear()
                            (it.obj as ArrayList<CourseInfo>).apply {
                                val today = Calendar.getInstance()[Calendar.DATE]
//                    用每个课程处于今天的时间排序，正确性有待观察
                                sortBy {
                                    if (it.courseStrTime1.date == today)
                                        it.courseStrTime1
                                    else
                                        it.courseStrTime2
                                }
                            }.forEach { add(it) }
                        }
                        this@ListRemoteViewsService.sendBroadcast(
                            Intent().setPackage(this@ListRemoteViewsService.packageName)
                                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        )
                        Log.i(
                            "widget",
                            "update app widget dataset finished , size=${todayCourseInfoList.size}"
                        )
                    }
                }
                true
            }).get())

            override fun initData() {
                val user = loadUser(this@ListRemoteViewsService)
                val firstWeek = loadFirstWeek(this@ListRemoteViewsService)
                if (user != null && firstWeek != null) {
                    getDayCourse(user, getThisWeek(firstWeek), Calendar.getInstance(), handler)
                }
            }

            override fun getActiveItem(): String {
                val now = Calendar.getInstance()
                val strTime = Calendar.getInstance()
                val endTime = Calendar.getInstance()
                return todayCourseInfoList.firstOrNull {
                    now.after(strTime.apply { time = it.courseStrTime1 })
                            && now.before(endTime.apply { time = it.courseEndTime1 })
                }?.courseName ?:"无"
            }

            override fun refresh() {
                initData()
//                getActiveItem()
            }

            override fun onCreate() {
                Log.i("widget", "start update app widget dataset")
                initData()
            }

            override fun onDataSetChanged() {

            }

            override fun onDestroy() {
                todayCourseInfoList.clear()
            }

            override fun getCount(): Int = todayCourseInfoList.size


            override fun getViewAt(position: Int): RemoteViews {
                return RemoteViews(
                    this@ListRemoteViewsService.packageName,
                    R.layout.desktop_list_view_item
                ).apply {
                    val courseInfo = todayCourseInfoList[position]
                    val now = Calendar.getInstance()
                    setTextViewText(R.id.desktop_course_name, courseInfo.courseName)
                    setTextViewText(R.id.desktop_course_location, courseInfo.location)
                    val simpleDateFormat = SimpleDateFormat("HH:mm")
                    if (courseInfo.courseStrTime1.date == now[Calendar.DATE]) {
                        setTextViewText(
                            R.id.desktop_course_time,
                            "${simpleDateFormat.format(courseInfo.courseStrTime1)}\n" + simpleDateFormat.format(
                                courseInfo.courseEndTime1
                            )
                        )
                        val courseLong = TimeUnit.MINUTES.convert(
                            courseInfo.courseEndTime1.time - courseInfo.courseStrTime1.time,
                            TimeUnit.MILLISECONDS
                        ).toInt()
                        val courseProgress = TimeUnit.MINUTES.convert(
                            now.time.time - courseInfo.courseStrTime1.time,
                            TimeUnit.MILLISECONDS
                        ).toInt()
                        setProgressBar(
                            R.id.desktopProgressBar,
                            courseLong,
                            if (courseProgress < 0) 0 else if (courseProgress > courseLong) courseLong else courseProgress,
                            false
                        )
                    } else {
                        setTextViewText(
                            R.id.desktop_course_time,
                            "${simpleDateFormat.format(courseInfo.courseStrTime2)}\n" + simpleDateFormat.format(
                                courseInfo.courseEndTime2
                            )
                        )
                        val courseLong = TimeUnit.MINUTES.convert(
                            courseInfo.courseEndTime2!!.time - courseInfo.courseStrTime2!!.time,
                            TimeUnit.MILLISECONDS
                        ).toInt()
                        val courseProgress = TimeUnit.MINUTES.convert(
                            now.time.time - courseInfo.courseStrTime2.time,
                            TimeUnit.MILLISECONDS
                        ).toInt()
                        setProgressBar(
                            R.id.desktopProgressBar,
                            courseLong,
                            if (courseProgress < 0) 0 else if (courseProgress > courseLong) courseLong else courseProgress,
                            false
                        )
                    }
                }
            }

            override fun getLoadingView(): RemoteViews? {
                return null
            }

            override fun getViewTypeCount(): Int = 1

            override fun getItemId(position: Int): Long = position.toLong()

            override fun hasStableIds(): Boolean = true
        }
        return listRemoteViewsFactory
    }
}
