package com.eilas.newcourseschedule.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.*
import com.eilas.newcourseschedule.data.model.LoggedInUser
import com.eilas.newcourseschedule.data.model.User
import com.eilas.newcourseschedule.databinding.ActivityLoginBinding
import com.eilas.newcourseschedule.databinding.AlertLoginRegisterBinding
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
                loggedInUser = loggedInUser.copy(name = it.obj as String)
                println(loggedInUser)
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
        setContentView(activityLoginBinding.let {
            textIsNotEmpty(it.id, it.pwd, unclickedView = it.btnLogin)
            it.root
        })

        autoLogin(this, activityLoginBinding.btnLogin)

        activityLoginBinding.btnLogin.setOnClickListener {
            loggedInUser = LoggedInUser(
                activityLoginBinding.id.editText?.text.toString(),
                activityLoginBinding.pwd.editText?.text.toString()
            )

            login(loggedInUser, handler)
        }

    }

    fun showRegisterDialog() {
//        弹框
        AlertDialog.Builder(this)
            .setTitle("用户不存在")
            .setMessage("注册？")
            .setView(alertLoginRegisterBinding.let {
                it.root.apply { (parent as ViewGroup?)?.removeAllViews() }
            })
            .setPositiveButton("是") { dialog, which ->
                loggedInUser = LoggedInUser(
                    activityLoginBinding.id.editText?.text.toString(),
                    activityLoginBinding.pwd.editText?.text.toString(),
                    alertLoginRegisterBinding.textName.editText?.text.toString().apply {
//                        未输入姓名则使用id
                        if (length == 0)
                            activityLoginBinding.id.editText?.text.toString()
                    },
                    if (alertLoginRegisterBinding.groupSex.checkedRadioButtonId == R.id.radioButton_male)
                        User.Sex.MALE
                    else
                        User.Sex.FEMALE
                )

                register(loggedInUser, handler)
            }
            .setNegativeButton("否", null)
            .setCancelable(false)
            .show()
            .getButton(AlertDialog.BUTTON_POSITIVE).let {
                textIsNotEmpty(alertLoginRegisterBinding.textName, unclickedView = it)
            }
    }

    override fun onDestroy() {
        super.onDestroy()

//        防止内存溢出
        handler.removeCallbacksAndMessages(null)
    }
}