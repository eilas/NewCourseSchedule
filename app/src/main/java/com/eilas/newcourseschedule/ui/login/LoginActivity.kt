package com.eilas.newcourseschedule.ui.login

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.login
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.data.register
import com.eilas.newcourseschedule.ui.schedule.CourseScheduleActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login_register.*

class LoginActivity : AppCompatActivity() {

    companion object {
        var handle: Handler? = null
    }

    lateinit var loggedInUser: LoggedInUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        autoLogin(this)

        btn_login.setOnClickListener {
            loggedInUser = LoggedInUser(id.text.toString(), pwd.text.toString())
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
                    1 -> Thread {
                        register(obj as LoggedInUser)
                    }.start()

//                    登录
                    2 -> Thread {
                        login(obj as LoggedInUser)
                    }.start()


//                    登录/注册成功
                    3 -> {
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        saveUser(this@LoginActivity, loggedInUser)
                        startActivity(
                            Intent(this@LoginActivity, CourseScheduleActivity::class.java).setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            ).putExtras(Bundle().let {
                                it.putParcelable(
                                    "user",
                                    loggedInUser as Parcelable
                                )
                                it
                            })
                        )
                    }

//                    pwd错误
                    4 -> {
                        Toast.makeText(this@LoginActivity, "密码错误！", Toast.LENGTH_SHORT).show()
                    }

//                    无用户
                    5 -> {
                        var inflate = View.inflate(
                            this@LoginActivity,
                            R.layout.activity_login_register,
                            null
                        )

//                        弹框
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("用户不存在")
                            .setMessage("注册？")
                            .setView(inflate)
                            .setPositiveButton(
                                "是",
                                DialogInterface.OnClickListener { dialog, which ->
                                    loggedInUser = LoggedInUser(
                                        id.text.toString(),
                                        pwd.text.toString(),
                                        inflate.findViewById<EditText>(R.id.text_name).text.toString()
                                            .let {
//                                                TODO:name判空使用其他方式
                                                if (it.length == 0)
                                                    id.text.toString()
                                                else
                                                    it
                                            },
                                        if (inflate.findViewById<RadioGroup>(R.id.group_sex).checkedRadioButtonId == R.id.radioButton_male)
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
                            .setCancelable(false)
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

//        防止内存溢出
        handle?.removeCallbacksAndMessages(null)
    }
}