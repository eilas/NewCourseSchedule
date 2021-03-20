package com.eilas.newcourseschedule

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test() {
        println(人类("张三") + 人类("李四"))
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