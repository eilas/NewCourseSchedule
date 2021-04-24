package com.eilas.newcourseschedule.data

import android.os.Handler
import android.os.Message
import com.google.gson.JsonParser
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.text.SimpleDateFormat
import java.util.*

object MQHelpers {
    val serverUrl: String = "tcp://139.224.33.253:1883"
    lateinit var client: MqttClient

    fun init(userId: String) {
        client = MqttClient(serverUrl, "consumer_$userId", MemoryPersistence())
    }

    fun connect(topic: String, handler: Handler) {
        Thread {
            kotlin.runCatching {
                client.apply {
                    connect(MqttConnectOptions().apply {
                        isCleanSession = false
                        userName = "guest"
                        password = "guest".toCharArray()
                    })
                    setCallback(object : MqttCallback {
                        override fun connectionLost(p0: Throwable?) {

                        }

                        override fun messageArrived(p0: String?, p1: MqttMessage?) {
                            receiveNotify()
                            println("mqtt consume topic:$p0 message:${p1?.let { String(it.payload) }}")
                            handler.sendMessage(Message.obtain().apply {
                                what = 2
                                obj = p1?.let {
                                    val jsonObject =
                                        JsonParser().parse(String(it.payload)).asJsonObject
                                    "${jsonObject["fromUserName"].asString}在${
                                        SimpleDateFormat("MM-dd HH:mm:ss").format(
                                            Date().apply { time = jsonObject["time"].asLong })
                                    }提醒你参加${jsonObject["courseName"].asString}"
                                }
                            })
                        }

                        override fun deliveryComplete(p0: IMqttDeliveryToken?) {

                        }

                    })
                    subscribe(topic, 1)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }.start()
    }

    fun shutDown() {
        client.apply {
            disconnect()
            close()
        }
    }
}