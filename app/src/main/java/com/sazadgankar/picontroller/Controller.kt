package com.sazadgankar.picontroller

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.Socket

class Controller(private val address: InetAddress) : HandlerThread("Controller") {
    companion object {
        const val TAG = "Controller"
    }

    var handler: Handler? = null
    private var socket: Socket? = null

    override fun onLooperPrepared() {
        try {
            connect()
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    try {
                        socket?.run {
                            Log.i(TAG, "Sending: " + msg.obj.toString())
                            outputStream.run {
                                write(msg.obj.toString().toByteArray(Charsets.US_ASCII))
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