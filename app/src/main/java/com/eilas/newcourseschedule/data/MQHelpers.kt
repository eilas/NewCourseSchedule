package com.eilas.newcourseschedule.data

import android.os.Handler
import android.os.Message
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

object MQHelpers {
    val serverUrl: String = "tcp://139.224.33.253:1883"
    lateinit var client: MqttAsyncClient

    fun init(userId: String) {
        client = MqttAsyncClient(serverUrl, "consumer_$userId", MemoryPersistence())
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
                                obj = p1?.let { String(it.payload) }
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