package com.eilas.newcourseschedule

import com.eilas.newcourseschedule.data.HttpHelpers
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test() {
        println(
            HttpHelpers.obtain().okHttpClient.newCall(
                Request.Builder().post(
                    Gson().toJson(人类("张三")).toRequestBody("application/json".toMediaTypeOrNull())
                ).url("http://192.168.1.199:8080/notify").build()
            ).execute()
        )
    }


    class 人类(val 姓名: String) {
        infix fun 加(人: 人类): String = "$姓名 ${人.姓名}"

        operator fun plus(人: 人类): String {
//            return this 加 人
            return 人 加 this
//            StackOverflowError
//            return 人 + this
        }
    }
}