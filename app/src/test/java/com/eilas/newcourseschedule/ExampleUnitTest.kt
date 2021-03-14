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
        val list = listOf<Int>(0, 1, 2, 3, 3, 4, 5, 6, 6, 7, 8, 8)
        val hashMap = HashMap<Int, Int>()
        for (i in list.indices) {
            if (i != 0 && list[i] == list[i - 1])
                continue
            hashMap.put(list[i],i)
        }
        println(hashMap)

    }
}