package com.eilas.newcourseschedule

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test() {
        val array = Array(13) { Array<String>(8, { i: Int -> i.toString() }) }
        for (i in array) {
            for (j in i)
                print(j)
            println()
        }
    }
}