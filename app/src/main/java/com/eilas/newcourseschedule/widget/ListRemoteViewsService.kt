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

class ListRemoteViewsService : RemoteViewsService() {
    interface ListRemoteViewsFactory : RemoteViewsFactory {
        fun initData()
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
                            "update app widget dataset finished , count=${todayCourseInfoList.size}"
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

            override fun refresh() {
                initData()
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
                    setTextViewText(
                        R.id.desktop_course_time,
                        if (courseInfo.courseStrTime1.date == now[Calendar.DATE])
                            "${simpleDateFormat.format(courseInfo.courseStrTime1)}  ~\n" +
                                    "      ${simpleDateFormat.format(courseInfo.courseEndTime1)}".also {
                                        if (now.time.after(courseInfo.courseStrTime1) &&
                                            now.time.before(courseInfo.courseEndTime1)
                                        )
                                            setProgressBar(R.id.progressBar2, 0, 0, true)
                                    }
                        else
                            "${simpleDateFormat.format(courseInfo.courseStrTime2)}  ~\n" +
                                    "      ${simpleDateFormat.format(courseInfo.courseEndTime2)}".also {
                                        if (now.time.after(courseInfo.courseStrTime2) &&
                                            now.time.before(courseInfo.courseEndTime2)
                                        )
                                            setProgressBar(R.id.progressBar2, 0, 0, true)
                                    }
                    )
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
