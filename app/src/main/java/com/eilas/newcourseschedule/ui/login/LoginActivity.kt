package com.eilas.newcourseschedule.ui.login

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import com.eilas.newcourseschedule.ui.schedule.CourseScheduleActivity

class LoginActivity : AppCompatActivity() {

    companion object {
        var handle: Handler? = null
    }

    lateinit var loggedInUser: LoggedInUser
    private lateinit var activityLoginBinding: ActivityLoginBinding
    private lateinit var alertLoginRegisterBinding: AlertLoginRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        alertLoginRegisterBinding = AlertLoginRegisterBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)

        autoLogin(this)

        activityLoginBinding.btnLogin.setOnClickListener {
            loggedInUser = LoggedInUser(
                activityLoginBinding.id.text.toString(),
                activityLoginBinding.pwd.text.toString()
            )
            handle?.sendMessage(Message.obtain().let {
                it.what = 2
                it.obj = loggedInUser
                it
            })
        }

//        handle为null则创建一个handle
        handle = handle ?: object : Handler() {
            override fun handleMessage(msg: Message) {
                val obj = msg.obj
                when (msg.what) {
//                    注册
                    1 -> register(obj as LoggedInUser)

//                    登录
                    2 -> login(obj as LoggedInUser)

//                    登录/注册成功
                    3 -> {
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        saveUser(this@LoginActivity, loggedInUser)
                        startActivity(
                            Intent(this@LoginActivity, CourseScheduleActivity::class.java).setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            ).putExtras(Bundle().apply {
                                putParcelable(
                                    "user",
                                    loggedInUser as Parcelable
                                )
                            })
                        )
                    }

//                    pwd错误
                    4 -> {
                        Toast.makeText(this@LoginActivity, "密码错误！", Toast.LENGTH_SHORT).show()
                    }

//                    无用户
                    5 -> {
                        alertLoginRegisterBinding.root.parent?.apply {
                            this as ViewGroup
                            removeAllViews()
                        }
//                        弹框
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("用户不存在")
                            .setMessage("注册？")
                            .setView(alertLoginRegisterBinding.root)
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
                                    sendMessage(Message.obtain().let {
                                        it.what = 1
                                        it.obj = loggedInUser
                                        it
                                    })
                                })
                            .setNegativeButton("否", null)
                            .setCancelable(false).apply {
                                if (!this@LoginActivity.isFinishing)
                                    show()
                            }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

//        防止内存溢出
        handle?.removeCallbacksAndMessages(null)
        handle = null
    }
}