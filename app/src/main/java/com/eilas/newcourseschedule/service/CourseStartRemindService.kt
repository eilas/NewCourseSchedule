package com.eilas.newcourseschedule.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.model.CourseInfo
import java.util.*
import java.util.concurrent.TimeUnit

class CourseStartRemindService : Service() {
    private val courseListBinder = CourseListBinder()

    inner class CourseListBinder : Binder() {
        var courseList: List<CourseInfo>? = null
            set(value) {
                if (courseList != null) {

                    throw Exception("虽然courseList是var,但不可以再次引用其他对象")
                } else {
                    field = value
                }
            }
        var courseDuration: Long = 15
        var dayStartTime: Calendar? = null
        var dayEndTime: Calendar? = null
        fun getTodayNextCourse(): CourseInfo? {
            val now = Calendar.getInstance()
            return kotlin.runCatching {
                courseListBinder.courseList!!.first {
                    it.courseStrTime1.day + 1 == now.get(Calendar.DAY_OF_WEEK) && Calendar.getInstance()
                        .apply {
                            time = it.courseStrTime1
                            set(now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DATE])
                        }.after(now) || it.courseStrTime2?.let {
                        it.day + 1 == now.get(Calendar.DAY_OF_WEEK) && Calendar.getInstance()
                            .apply {
                                time = it
                                set(now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DATE])
                            }.after(now)
                    } ?: false
                }
            }.getOrNull().also { Log.i("CourseStartRemindService", it.toString()) }
        }
    }

    inner class RemindWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
        override fun doWork(): Result {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                1, NotificationCompat.Builder(this@CourseStartRemindService, "0")
                    .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("课前提醒")
                    .setContentText("课程\"${inputData.getString("name")}\"即将开始").build()
            )

            val now = Calendar.getInstance()
            var todayNextCourseStrName: String = ""
            val todayNextCourseStrTime = courseListBinder.getTodayNextCourse()
                ?.also { todayNextCourseStrName = it.courseName }?.let {
                    if (it.courseStrTime1.day == now.get(Calendar.DAY_OF_WEEK))
                        it.courseStrTime1
                    else
                        it.courseStrTime2
                }

            todayNextCourseStrTime?.apply {
                val remindWorkerRequest = OneTimeWorkRequestBuilder<RemindWorker>().setInitialDelay(
//                    提醒时间课前10min
                    time - now.timeInMillis - 600000,
                    TimeUnit.MILLISECONDS
                ).setInputData(Data.Builder().putString("name", todayNextCourseStrName).build())
                    .build()

                WorkManager.getInstance(this@CourseStartRemindService).enqueue(remindWorkerRequest)
            }


            return Result.success()
        }
    }

    inner class WatchDogWorker(context: Context, params: WorkerParameters) :
        Worker(context, params) {
        override fun doWork(): Result {
            val now = Calendar.getInstance()
            val nextDay = Calendar.getInstance().let {
                it.set(Calendar.HOUR_OF_DAY, 7)
                it.set(Calendar.MINUTE, 30)
                it.set(Calendar.SECOND, 0)
                if (it.before(now)) {
                    it.add(Calendar.HOUR_OF_DAY, 24)
                }
                it
            }

            var todayNextCourseStrName: String = ""
            val todayNextCourseStrTime = courseListBinder.getTodayNextCourse()
                ?.also { todayNextCourseStrName = it.courseName }?.let {
                    if (it.courseStrTime1.day == now.get(Calendar.DAY_OF_WEEK))
                        it.courseStrTime1
                    else
                        it.courseStrTime2
                }!!
            val remindWorkerRequest = OneTimeWorkRequestBuilder<RemindWorker>().setInitialDelay(
//                提醒时间课前10min
                todayNextCourseStrTime.time - now.timeInMillis - 600000, TimeUnit.MILLISECONDS
            ).setInputData(Data.Builder().putString("name", todayNextCourseStrName).build()).build()

            val watchDogWorkerRequest = OneTimeWorkRequestBuilder<WatchDogWorker>().setInitialDelay(
                nextDay.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS
            ).build()

            WorkManager.getInstance(this@CourseStartRemindService).apply {
                enqueue(remindWorkerRequest)
                enqueue(watchDogWorkerRequest)
            }
            return Result.success()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return courseListBinder
    }

    override fun onCreate() {
        Log.i("CourseStartRemindService", "create")
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel("0", "课前提醒", NotificationManager.IMPORTANCE_DEFAULT)
        )
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("CourseStartRemindService", "start")

        val now = Calendar.getInstance()
        val watchDogWorkRequest = OneTimeWorkRequestBuilder<WatchDogWorker>().setInitialDelay(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                if (before(now)) {
                    add(Calendar.HOUR_OF_DAY, 24)
                }
            }.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS
        ).build()

        if (now.after(courseListBinder.dayStartTime) && now.before(courseListBinder.dayEndTime)) {
//            位于可能有课的时间，需要执行RemindWorker
            Log.i("CourseStartRemindService", "both RemindWorker WatchDogWorker")
            var todayNextCourseStrName: String = ""
            val todayNextCourseStrTime = courseListBinder.getTodayNextCourse()
                ?.also { todayNextCourseStrName = it.courseName }?.let {
                    if (it.courseStrTime1.day == now.get(Calendar.DAY_OF_WEEK))
                        it.courseStrTime1
                    else
                        it.courseStrTime2
                }

            todayNextCourseStrTime?.apply {
                WorkManager.getInstance(this@CourseStartRemindService).enqueue(
                    OneTimeWorkRequestBuilder<RemindWorker>().setInitialDelay(
                        time - now.timeInMillis - 600000,
                        TimeUnit.MILLISECONDS
                    ).setInputData(Data.Builder().putString("name", todayNextCourseStrName).build())
                        .build()
                )
            }
        } else {
            Log.i("CourseStartRemindService", "only WatchDogWorker")
        }
//        无论是今天还是明天，WatchDogWorker总会执行
        WorkManager.getInstance(this).enqueue(watchDogWorkRequest)

        return super.onStartCommand(intent, flags, startId)
    }
}