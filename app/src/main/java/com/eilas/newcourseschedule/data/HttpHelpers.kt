package com.eilas.newcourseschedule.data

import androidx.core.util.Pools.SynchronizedPool
import okhttp3.OkHttpClient

class HttpHelpers {
    companion object {
        val pool = SynchronizedPool<HttpHelpers>(4)

        fun obtain(): HttpHelpers = pool.acquire() ?: HttpHelpers()
    }

    fun recycle() {
        pool.release(this)
    }

    val url: String = "http://192.168.1.199:8080"
    val okHttpClient: OkHttpClient = OkHttpClient()

    private constructor()
}
