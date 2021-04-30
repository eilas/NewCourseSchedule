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
import com.google.gson.Gson
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CourseStartRemindService : Service() {
    companion object {
        lateinit var courseListBinder: CourseListBinder

        fun writeCourseListBinder(context: Context): Boolean {
            try {
                context.openFileOutput("courseListBinder", Context.MODE_PRIVATE).apply {
                    write(Gson().toJson(courseListBinder).toByteArray())
                    flush()
                    close()
                }
            } catch (e: IOException) {
                return false
            }
            return true
        }

        fun readCourseListBinder(context: Context): Boolean {
            try {
                context.openFileInput("courseListBinder").apply {
                    courseListBinder =
                        Gson().fromJson(reader().readText(), CourseListBinder::class.java)
                    close()
                }
            } catch (e: IOException) {
                return false
            }
            return true
        }

    }

    init {
        courseListBinder = CourseListBinder()
    }

    inner class CourseListBinder : Binder() {
        var courseList: List<CourseInfo>? = null
            set(value) {
                if (courseList != null) {

                    throw Exception("虽然courseList是var,但不可以再次引用其他对象")
                } else {
                    field = value
                }
            }
        var dayStartTime: Calendar? = null
        var dayEndTime: Calendar? = null

        /**
         * 获取当前时间的下一节课程，如果课程的开始时间距今少于excludedDuration,则顺延至下个课程
         * @param excludedDuration 时间段(ms),默认600000ms <=> 10min
         * @return 下一节课程
         */
        fun getTodayNextCourse(excludedDuration: Long = 600000): CourseInfo? {
            val now = Calendar.getInstance()
            return kotlin.runCatching {
                courseList!!.first {
                    it.courseStrTime1.day + 1 == now.get(Calendar.DAY_OF_WEEK) && Calendar.getInstance()
                        .apply {
                            time = it.courseStrTime1
                            set(now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DATE])
                            add(
                                Calendar.MINUTE,
                                -TimeUnit.MINUTES.convert(excludedDuration, TimeUnit.MILLISECONDS)
                                    .toInt()
                            )
                        }.after(now) || it.courseStrTime2?.let {
                        it.day + 1 == now.get(Calendar.DAY_OF_WEEK) && Calendar.getInstance()
                            .apply {
                                time = it
                                set(now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DATE])
                                add(
                                    Calendar.MINUTE,
                                    -TimeUnit.MINUTES.convert(
                                        excludedDuration,
                                        TimeUnit.MILLISECONDS
                                    ).toInt()
                                )
                            }.after(now)
                    } ?: false
                }
            }.getOrNull().also { Log.i("CourseStartRemindService_NextCourse", it.toString()) }
        }

        fun notifyDataListChanged() {
            Log.i("CourseStartRemindService", "数据更新")
            val now = Calendar.getInstance()
            var todayNextCourseName: String = ""
            val todayNextCourseStrTime = getTodayNextCourse()?.let {
                todayNextCourseName = it.courseName
                it.getCourseStrTimeToday()
            }

            WorkManager.getInstance(this@CourseStartRemindService).apply {
                cancelAllWorkByTag("课前提醒")

                todayNextCourseStrTime?.apply {
                    enqueue(
                        OneTimeWorkRequestBuilder<RemindWorker>().setInitialDelay(
                            time - now.timeInMillis - 600000,
                            TimeUnit.MILLISECONDS
                        ).setInputData(
                            Data.Builder().putString("name", todayNextCourseName).build()
                        ).addTag("课前提醒").build()
                    )
                }
            }

            Log.i(
                "CourseStartRemindService",
                "课前提醒已更新，预计下次${
                    TimeUnit.MINUTES.convert(
                        (todayNextCourseStrTime?.time ?: 0) - now.timeInMillis - 600000,
                        TimeUnit.MILLISECONDS
                    )
                }min后"
            )
        }
    }

    class RemindWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
        override fun doWork(): Result {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                1,
                NotificationCompat.Builder(context, "0")
                    .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("课前提醒")
                    .setContentText("课程\"${inputData.getString("name")}\"即将开始").build()
            )

            val now = Calendar.getInstance()
            var todayNextCourseStrName: String = ""
            val todayNextCourseStrTime =
                kotlin.runCatching {
                    courseListBinder.getTodayNextCourse()
                }.onFailure {
                    readCourseListBinder(context)
                }.getOrElse {
                    courseListBinder.getTodayNextCourse()
                }?.let {
                    todayNextCourseStrName = it.courseName
                    it.getCourseStrTimeToday()
                }


            todayNextCourseStrTime?.apply {
                val remindWorkerRequest = OneTimeWorkRequestBuilder<RemindWorker>().setInitialDelay(
//                    提醒时间课前10min
                    time - now.timeInMillis - 600000,
                    TimeUnit.MILLISECONDS
                ).setInputData(Data.Builder().putString("name", todayNextCourseStrName).build())
                    .addTag("课前提醒").build()

                WorkManager.getInstance(context).enqueue(remindWorkerRequest)
            }

            context.openFileOutput("Remind_RunTime", Context.MODE_APPEND).apply {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                write(
                    "${simpleDateFormat.format(now.time)} ~ 提醒服务运行，下一次预计时间${
                        todayNextCourseStrTime?.let {
                            simpleDateFormat.format(Calendar.getInstance().apply {
                                time = it
                                add(Calendar.MINUTE, -10)
                            }.time)
                        }
                    }\n".toByteArray()
                )
                flush()
                close()
            }

            return Result.success()
        }
    }

    class WatchDogWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
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
            val todayNextCourseStrTime =
                kotlin.runCatching {
                    courseListBinder.getTodayNextCourse()
                }.onFailure {
                    readCourseListBinder(context)
                }.getOrElse {
                    courseListBinder.getTodayNextCourse()
                }?.let {
                    todayNextCourseStrName = it.courseName
                    it.getCourseStrTimeToday()
                }

            val remindWorkerRequest = todayNextCourseStrTime?.let {
                OneTimeWorkRequestBuilder<RemindWorker>().setInitialDelay(
//                    提醒时间课前10min
                    it.time - now.timeInMillis - 600000, TimeUnit.MILLISECONDS
                ).setInputData(Data.Builder().putString("name", todayNextCourseStrName).build())
                    .addTag("课前提醒").build()
            }


            val watchDogWorkerRequest = OneTimeWorkRequestBuilder<WatchDogWorker>().setInitialDelay(
                nextDay.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS
            ).addTag("看门狗").build()

            WorkManager.getInstance(context).apply {
                remindWorkerRequest?.let { enqueue(it) }
                enqueue(watchDogWorkerRequest)
            }

            context.openFileOutput("WatchDog_RunTime", Context.MODE_APPEND).apply {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                write(
                    "${simpleDateFormat.format(now.time)} ~ 看门狗服务运行，下一次预计时间${
                        simpleDateFormat.format(
                            nextDay
                        )
                    }\n".toByteArray()
                )
                flush()
                close()
            }

            return Result.success()
        }
    }

    override fun onCreate() {
        Log.i("CourseStartRemindService", "create")
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel("0", "课前提醒", NotificationManager.IMPORTANCE_DEFAULT)
        )
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        return courseListBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("CourseStartRemindService", "start")
//        WorkManager.getInstance(this).cancelAllWork()

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
        ).addTag("看门狗").build()

        if (now.after(courseListBinder.dayStartTime) && now.before(courseListBinder.dayEndTime)) {
//            位于可能有课的时间，需要执行RemindWorker
            Log.i("CourseStartRemindService", "both RemindWorker WatchDogWorker")
            var todayNextCourseName: String = ""
            val todayNextCourseStrTime = courseListBinder.getTodayNextCourse()?.let {
                todayNextCourseName = it.courseName
                it.getCourseStrTimeToday()
            }

            todayNextCourseStrTime?.apply {
                WorkManager.getInstance(this@CourseStartRemindService).enqueue(
                    OneTimeWorkRequestBuilder<RemindWorker>().setInitialDelay(
                        time - now.timeInMillis - 600000,
                        TimeUnit.MILLISECONDS
                    ).setInputData(Data.Builder().putString("name", todayNextCourseName).build())
                        .addTag("课前提醒").build()
                )
            }
        } else {
            Log.i("CourseStartRemindService", "only WatchDogWorker")
        }

//        无论是今天还是明天，WatchDogWorker总会执行
        WorkManager.getInstance(this).enqueue(watchDogWorkRequest)

/*
        WorkManager.getInstance(this).enqueue(
            PeriodicWorkRequestBuilder<Test>(60, TimeUnit.MINUTES).build()
        )
*/


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i("CourseStartRemindService", "unbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.i("CourseStartRemindService", "destroy")
//        将courseListBinder写入本地文件
        writeCourseListBinder(this)
        super.onDestroy()
    }


    /**
     * test
     */
    class Test(val context: Context, params: WorkerParameters) : Worker(context, params) {
        override fun doWork(): Result {
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                1, NotificationCompat.Builder(context, "0")
                    .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("TEST")
                    .setContentText("时间$time")
                    .build()
            )
            val edit = context.getSharedPreferences("ServiceStatus", Context.MODE_PRIVATE).edit()
            kotlin.runCatching {
                println(courseListBinder.getTodayNextCourse())
            }.onSuccess {
                edit.putString(time, "notify运行了,courseListBinder也能访问")
            }.onFailure {
                val s: StringBuilder = StringBuilder("notify运行了\n${it}")
                it.stackTrace.forEach { s.append("\n   at $it") }
                edit.putString(time, s.toString())
            }
            edit.commit()
            return Result.success()
        }
    }
}