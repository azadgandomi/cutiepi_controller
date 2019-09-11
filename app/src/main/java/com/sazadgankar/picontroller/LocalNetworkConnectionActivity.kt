package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.InetAddress


class LocalNetworkConnectionActivity : AppCompatActivity(), SurfaceHolder.Callback,
    NsdManager.DiscoveryListener {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.w("SurfaceHolder", "Surface Changed!")
        mjpegPlayer?.setDisplay(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.i("SurfaceHolder", "Surface Destroyed!")
        mjpegPlayer?.setDisplay(null)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.i("SurfaceHolder", "Surface Created!")
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
    private var controller: Controller? = null
    private var mjpegPlayer: MjpegPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
        surfaceView.holder.addCallback(this)
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
    }

    private fun connect(address: InetAddress) {
        val cThread = Controller(address)
        cThread.start()
        controller = cThread
        val player = MjpegPlayer()
        player.setDisplay(surfaceView.holder)
        player.start(address, PORT_CAMERA)
        mjpegPlayer = player
    }

    override fun onStop() {
        super.onStop()
        mjpegPlayer?.close()
        mjpegPlayer = null
        controller?.close()
        controller = null
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
                controller?.handler?.sendMessage(message)
            } else if (event.action == MotionEvent.ACTION_UP) {
                Log.v("TOUCH", "Up: $command")
                val message = Message.obtain()
                message.obj = "ST"
                controller?.handler?.sendMessage(message)
            }
            return false
        }
    }
}
