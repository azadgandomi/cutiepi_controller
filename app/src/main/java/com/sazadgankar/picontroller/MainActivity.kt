package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.net.InetAddress
import java.net.Socket


const val PI_IP = "192.168.43.88"
const val PORT_CONTROL = 56789
const val PORT_VIDEO = 8082


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.w("SurfaceHolder", "Surface Changed!")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.i("SurfaceHolder", "Surface Destroyed!")
        mediaPlayer.setDisplay(null)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.i("SurfaceHolder", "Surface Created!")
        mediaPlayer.setDisplay(holder)
    }

    private var commandThread: CommandThread? = null
    private val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        commandThread = CommandThread(PI_IP).apply { start() }
        surfaceView.holder.addCallback(this)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.release()
        commandThread?.close()
    }

    private fun setupButtons() {
        goForwardButton.setOnTouchListener(NavigationOnTouchListener("GF", commandThread))
        goBackwardButton.setOnTouchListener(NavigationOnTouchListener("GB", commandThread))
        turnRightButton.setOnTouchListener(NavigationOnTouchListener("TR", commandThread))
        turnLeftButton.setOnTouchListener(NavigationOnTouchListener("TL", commandThread))
        rotateClockwiseButton.setOnTouchListener(NavigationOnTouchListener("RC", commandThread))
        rotateAntiClockwiseButton.setOnTouchListener(NavigationOnTouchListener("RA", commandThread))
    }

    private class NavigationOnTouchListener(private val command: String, private val commandThread: CommandThread?) :
        View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val message = Message.obtain()
                message.obj = command
                commandThread?.handler?.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val message = Message.obtain()
                message.obj = "S"
                commandThread?.handler?.sendMessage(message)
            }
            return false
        }
    }

    private class CommandThread(private val address: String) : HandlerThread("CommandThread") {
        private var socket: Socket? = null
        var handler: Handler? = null

        override fun onLooperPrepared() {
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    try {
                        socket?.run {
                            Log.i("CommandThread", "Sending: " + msg.obj.toString())
                            outputStream.run {
                                write(msg.obj.toString().toByteArray(Charsets.US_ASCII))
                                flush()
                            }
                        }
                    } catch (exception: IOException) {
                        Log.w("CommandThread", exception)
                    }
                }
            }
            try {
                connect()
            } catch (exception: IOException) {
                Log.w("CommandThread", exception)
            }
        }

        private fun connect() {
            val address = InetAddress.getByName(address)
            Log.i("CommandThread", "Connecting...")
            socket = Socket(address, PORT_CONTROL)
            Log.i("CommandThread", "Connected!")
        }

        fun close() {
            quit()
            socket?.close()
        }
    }

}
