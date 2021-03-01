package com.eilas.newcourseschedule

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test() {
        println(Calendar.getInstance().time.apply {

            println(
                SimpleDateFormat("EEE-MMM-dd-HH:mm:ss-zzzzzzzz-yyyy").parse(
                    "Mon Mar 01 21:31:22 GMT+08:00 2021".replace(
                        " ",
                        "-"
                    )
                )
            )
        })
    }
}