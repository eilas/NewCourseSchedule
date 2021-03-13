package com.eilas.newcourseschedule.data

import androidx.core.util.Pools.SynchronizedPool
import okhttp3.OkHttpClient

class HttpHelper {
    companion object {
        val pool = SynchronizedPool<HttpHelper>(4)

        fun obtain(): HttpHelper = pool.acquire() ?: HttpHelper()
    }

    fun recycle() {
        pool.release(this)
    }

    val url: String = "http://192.168.1.102:8080"
    val okHttpClient: OkHttpClient = OkHttpClient()

    private constructor()
}
