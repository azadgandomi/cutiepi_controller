package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.graphics.Camera
import android.media.MediaPlayer
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileDescriptor
import java.io.IOException
import java.net.*
import android.webkit.WebView


const val PI_IP = "192.168.43.88"
const val PORT_CONTROL = 56789
//const val PORT_VIDEO = 45678


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

    private lateinit var commandThread: CommandThread
    private val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()
        commandThread = CommandThread("CommandThread", PI_IP)
        commandThread.start()
        webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    view.pageDown(true)
                }
            }
            loadUrl("http://192.168.43.88:8082/index.html")
        }
//        surfaceView.holder.addCallback(this)
//        mediaPlayer.apply {
//            setDataSource("http://192.168.43.88:45678/index.html")
//            setOnPreparedListener { mp -> mp.start() }
//            prepareAsync()
//        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.release()
        commandThread.close()
    }

    private fun setupButtons() {
        goForwardButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val message = Message()
                message.obj = "GF"
                commandThread.handler.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val message = Message()
                message.obj = "S"
                commandThread.handler.sendMessage(message)
            }
            false
        }

        goBackwardButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val message = Message()
                message.obj = "GB"
                commandThread.handler.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val message = Message()
                message.obj = "S"
                commandThread.handler.sendMessage(message)
            }
            false
        }

        turnRightButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val message = Message()
                message.obj = "TR"
                commandThread.handler.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val message = Message()
                message.obj = "S"
                commandThread.handler.sendMessage(message)
            }
            false
        }

        turnLeftButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val message = Message()
                message.obj = "TL"
                commandThread.handler.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val message = Message()
                message.obj = "S"
                commandThread.handler.sendMessage(message)
            }
            false
        }

        rotateClockwiseButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val message = Message()
                message.obj = "RC"
                commandThread.handler.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val message = Message()
                message.obj = "S"
                commandThread.handler.sendMessage(message)
            }
            false
        }

        rotateAntiClockwiseButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val message = Message()
                message.obj = "RA"
                commandThread.handler.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val message = Message()
                message.obj = "S"
                commandThread.handler.sendMessage(message)
            }
            false
        }
    }

    private class CommandThread(name: String, private val address: String) : HandlerThread(name) {
        private var socket: Socket? = null
        lateinit var handler: Handler

        override fun onLooperPrepared() {
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    try {
                        socket?.let {
                            if (it.isClosed or !it.isConnected) {
                                connect()
                            }
                            Log.i("CommandThread", "Sending: " + msg.obj.toString())
                            it.outputStream.write(msg.obj.toString().toByteArray(Charsets.US_ASCII))
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
            } catch (exception: SocketTimeoutException) {
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

//    private class VideoThread(name: String, private val address: String) : HandlerThread(name) {
//        private var socket: Socket? = null
//        lateinit var handler: Handler
//
//        override fun onLooperPrepared() {
//            handler = object : Handler(looper) {
//                override fun handleMessage(msg: Message) {
//                    super.handleMessage(msg)
//                    try {
//                        socket?.let {
//                            if (it.isClosed or !it.isConnected) {
//                                connect()
//                            }
//                            Log.i("CommandThread", "Sending: " + msg.obj.toString())
//                            it.outputStream.write(msg.obj.toString().toByteArray(Charsets.US_ASCII))
//                        }
//                    } catch (exception: IOException) {
//                        Log.w("CommandThread", exception)
//                    }
//                }
//            }
//            try {
//                connect()
//            } catch (exception: IOException) {
//                Log.w("CommandThread", exception)
//            } catch (exception: SocketTimeoutException) {
//                Log.w("CommandThread", exception)
//            }
//        }
//
//        private fun connect() {
//            val address = InetAddress.getByName(address)
//            Log.i("CommandThread", "Connecting...")
//            socket = Socket(address, PORT_VIDEO)
//            Log.i("CommandThread", "Connected!")
//        }
//
//        fun close() {
//            quit()
//            socket?.close()
//        }
//    }
}
