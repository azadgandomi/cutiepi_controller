package com.sazadgankar.picontroller

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.widget.PowerBar
import java.io.IOException
import java.net.InetAddress
import java.net.Socket

class Controller(private val address: InetAddress, private var powerBar: PowerBar) :
    HandlerThread("Controller") {
    companion object {
        const val TAG = "Controller"
    }

    var handler: Handler? = null
    private var socket: Socket? = null

    override fun onLooperPrepared() {
        try {
            connect()
            val power = socket?.getInputStream()?.read() ?: 0
            Log.i(TAG, "Default power is $power")
            powerBar.post {
                powerBar.progress = power
            }
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    try {
                        socket?.run {
                            Log.i(TAG, "Sending: " + (msg.obj as ByteArray).contentToString())
                            outputStream.run {
                                write(msg.obj as ByteArray)
                                flush()
                            }
                        }
                    } catch (exception: IOException) {
                        Log.w(TAG, exception)
                    }
                }
            }
        } catch (exception: IOException) {
            Log.w(TAG, exception)
            close()
        }
    }

    private fun connect() {
        Log.i(TAG, "Connecting...")
        socket = Socket(address, PORT_CONTROL)
        Log.i(TAG, "Connected!")
    }

    fun close() {
        quit()
        socket?.close()
    }
}