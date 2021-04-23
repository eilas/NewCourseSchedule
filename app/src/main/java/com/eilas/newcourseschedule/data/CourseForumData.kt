package com.eilas.newcourseschedule.data

import android.os.Handler
import android.os.Message
import com.eilas.newcourseschedule.data.model.CommentMessage
import com.eilas.newcourseschedule.data.model.ReplyMessage
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun getCourseForum(userId: String, courseId: String, refresh: Boolean = false, handler: Handler) {
    Thread {
        val gson = Gson()
        val httpHelper = HttpHelpers.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(mapOf("userId" to userId, "courseId" to courseId))
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/communication?action=search&method=all").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                handler.sendMessage(Message.obtain().apply {
                    what = if (refresh) 2 else 1
                    obj = response.body?.string()
                })
            }
        })

        httpHelper.recycle()
    }.start()
}

fun saveCourseComment(
    userId: String,
    courseId: String,
    comment: CommentMessage,
    handler: Handler
) {
    Thread {
        val gson = Gson()
        val httpHelper = HttpHelpers.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(mapOf("userId" to userId, "courseId" to courseId, "message" to comment))
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/communication?action=save&method=comment").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val messageId =
                    JsonParser().parse(response.body?.string()).asJsonObject["messageId"].asLong
                comment.id = messageId

/*
                handler.sendMessage(Message.obtain().apply {
                    what = 5
                    obj = messageId
                })
*/
            }

        })

        httpHelper.recycle()
    }.start()
}

fun saveCourseCommentReply(
    userId: String,
    courseId: String,
    reply: ReplyMessage,
    handler: Handler
) {
    Thread {
        val gson = Gson()
        val httpHelper = HttpHelpers.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(mapOf("userId" to userId, "courseId" to courseId, "message" to reply))
                    .toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/communication?action=save&method=reply").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val messageId =
                    JsonParser().parse(response.body?.string()).asJsonObject["messageId"].asLong
                reply.id = messageId

/*
                handler.sendMessage(Message.obtain().apply {
                    what = 5
                    obj = messageId
                })
*/
            }

        })

        httpHelper.recycle()
    }.start()
}

fun like(messageId: Long, courseId: String, userId: String, handler: Handler) {
    Thread {
        val gson = Gson()
        val httpHelper = HttpHelpers.obtain()

        httpHelper.okHttpClient.newCall(
            Request.Builder().post(
                gson.toJson(
                    mapOf("messageId" to messageId, "userId" to userId, "courseId" to courseId)
                ).toRequestBody("application/json".toMediaTypeOrNull())
            ).url(httpHelper.url + "/communication?action=update&method=like").build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {

            }

        })

        httpHelper.recycle()
    }.start()
}