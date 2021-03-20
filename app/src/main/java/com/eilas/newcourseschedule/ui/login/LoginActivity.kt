package com.eilas.newcourseschedule.ui.login

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.login
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.data.register
import com.eilas.newcourseschedule.databinding.ActivityLoginBinding
import com.eilas.newcourseschedule.databinding.AlertLoginRegisterBinding
import com.eilas.newcourseschedule.service.CourseStartRemindService
import com.eilas.newcourseschedule.ui.schedule.CourseScheduleActivity
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

class LoginActivity : AppCompatActivity() {

    lateinit var loggedInUser: LoggedInUser
    private lateinit var activityLoginBinding: ActivityLoginBinding
    private lateinit var alertLoginRegisterBinding: AlertLoginRegisterBinding
    val handler: Handler = Handler(WeakReference(Handler.Callback {
        when (it.what) {
//            登录/注册成功
            3 -> {
                saveUser(this, loggedInUser)
                startActivity(
                    Intent(
                        this,
                        CourseScheduleActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtras(Bundle().apply {
                            putParcelable(
                                "user",
                                loggedInUser as Parcelable
                            )
                        })
                )
//                startService(Intent(this, CourseStartRemindService::class.java))
            }
//            pwd错误
            4 -> {
                Toast.makeText(this, "密码错误！", Toast.LENGTH_SHORT).show()
            }
//            无用户
            5 -> showRegisterDialog()
//            其他情况
            6 -> Snackbar.make(
                activityLoginBinding.btnLogin,
                "登录失败~服务器无响应~",
                Snackbar.LENGTH_LONG
            ).show()
        }
        true
    }).get())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        alertLoginRegisterBinding = AlertLoginRegisterBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)

        autoLogin(this, activityLoginBinding.btnLogin)

        activityLoginBinding.btnLogin.setOnClickListener {
            loggedInUser = LoggedInUser(
                activityLoginBinding.id.text.toString(),
                activityLoginBinding.pwd.text.toString()
            )

            login(loggedInUser, handler)
        }

    }

    fun showRegisterDialog() {
//        弹框
        AlertDialog.Builder(this)
            .setTitle("用户不存在")
            .setMessage("注册？")
            .setView(alertLoginRegisterBinding.root.apply {
                parent?.apply {
                    (this as ViewGroup).removeAllViews()
                }
            })
            .setPositiveButton(
                "是",
                DialogInterface.OnClickListener { dialog, which ->
                    loggedInUser = LoggedInUser(
                        activityLoginBinding.id.text.toString(),
                        activityLoginBinding.pwd.text.toString(),
                        alertLoginRegisterBinding.textName.text.toString()
                            .apply {
//                                                未输入姓名则使用id
                                if (length == 0)
                                    activityLoginBinding.id.text.toString()
                            },
                        if (alertLoginRegisterBinding.groupSex.checkedRadioButtonId == R.id.radioButton_male)
                            LoggedInUser.Sex.MALE
                        else
                            LoggedInUser.Sex.FEMALE
                    )

                    register(loggedInUser, handler)
                })
            .setNegativeButton("否", null)
            .setCancelable(false)
            .apply {
                if (!this@LoginActivity.isFinishing)
                    show()
            }

    }

    override fun onDestroy() {
        super.onDestroy()

//        防止内存溢出
        handler.removeCallbacksAndMessages(null)
    }
}