package com.eilas.newcourseschedule.data

import okhttp3.OkHttpClient

object HttpHelper {
    val url: String = "http://192.168.0.107:8080"
    val okHttpClient: OkHttpClient = OkHttpClient()
}