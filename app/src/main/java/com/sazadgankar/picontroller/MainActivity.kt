package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
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
import java.net.SocketTimeoutException


const val PORT_CONTROL = 56789
const val PORT_CAMERA = 45678


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, NsdManager.DiscoveryListener {
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

    private var hostAddress: InetAddress? = null

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.i("DiscoveryListener", "Resolve failed: ${serviceInfo?.toString()}")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            Log.i("DiscoveryListener", "Resolved: ${serviceInfo?.toString()}")
            serviceInfo?.host?.let { address ->
                hostAddress = address
                Handler(mainLooper).post { connect(address) }

            }
        }
    }

    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
        Log.i("DiscoveryListener", "StopFailed: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
        Log.i("DiscoveryListener", "StartFailed: $serviceType")
    }

    override fun onDiscoveryStarted(serviceType: String?) {
        Log.i("DiscoveryListener", "Started: $serviceType")
    }

    override fun onDiscoveryStopped(serviceType: String?) {
        Log.i("DiscoveryListener", "Stopped: $serviceType")
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
        Log.i("DiscoveryListener", "Lost: ${serviceInfo?.toString()}")
    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
        Log.i("DiscoveryListener", "Found: ${serviceInfo?.toString()}")
        nsdManager.stopServiceDiscovery(this)
        nsdManager.resolveService(serviceInfo, resolveListener)
    }

    private lateinit var nsdManager: NsdManager
    private var commandThread: CommandThread? = null
    private val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    override fun onStart() {
        super.onStart()
        hostAddress.let { address ->
            if (address != null) {
                connect(address)
            } else {
                nsdManager.discoverServices("_qtpie._tcp.", NsdManager.PROTOCOL_DNS_SD, this)
            }
        }
        // TODO: surfaceView.holder.addCallback(this)
    }

    private fun connect(address: InetAddress) {
        commandThread = CommandThread(address)
        commandThread!!.start()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.release()
        commandThread?.close()
        try {
            nsdManager.stopServiceDiscovery(this)
        } catch (exception: IllegalArgumentException) {
        }
    }

    private fun setupButtons() {
        goForwardButton.setOnTouchListener(NavigationOnTouchListener("GF"))
        goBackwardButton.setOnTouchListener(NavigationOnTouchListener("GB"))
        turnRightButton.setOnTouchListener(NavigationOnTouchListener("TR"))
        turnLeftButton.setOnTouchListener(NavigationOnTouchListener("TL"))
        rotateClockwiseButton.setOnTouchListener(NavigationOnTouchListener("RC"))
        rotateAntiClockwiseButton.setOnTouchListener(NavigationOnTouchListener("RA"))
    }

    private inner class NavigationOnTouchListener(private val command: String) :
        View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.v("TOUCH", "Down: $command")
                val message = Message.obtain()
                message.obj = command
                commandThread?.handler?.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                Log.v("TOUCH", "Up: $command")
                val message = Message.obtain()
                message.obj = "S"
                commandThread?.handler?.sendMessage(message)
            }
            return false
        }
    }

    private class CommandThread(private val address: InetAddress) : HandlerThread("CommandThread") {
        companion object {
            const val Tag = "CommandThread"
        }

        var handler: Handler? = null
        private var socket: Socket? = null

        override fun onLooperPrepared() {
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    try {
                        socket?.run {
                            if (isClosed or !isConnected) {
                                Log.w(Tag, "Socket is closed!")
                                connect()
                            }
                            Log.i(Tag, "Sending: " + msg.obj.toString())
                            outputStream.run {
                                write(msg.obj.toString().toByteArray(Charsets.US_ASCII))
                                flush()
                            }
                        }
                    } catch (exception: IOException) {
                        Log.w(Tag, exception)
                    }
                }
            }
            try {
                connect()
            } catch (exception: IOException) {
                Log.w(Tag, exception)
            } catch (exception: SocketTimeoutException) {
                Log.w(Tag, exception)
            }
        }

        private fun connect() {
            Log.i(Tag, "Connecting...")
            socket = Socket(address, PORT_CONTROL)
            Log.i(Tag, "Connected!")
        }

        fun close() {
            quit()
            socket?.close()
        }
    }
}
