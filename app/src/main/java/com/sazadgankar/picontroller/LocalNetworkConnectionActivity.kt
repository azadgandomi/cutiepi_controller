package com.sazadgankar.picontroller

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
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
        surfaceHolder = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.i("SurfaceHolder", "Surface Created!")
        surfaceHolder = holder
    }

    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {}
    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {}
    override fun onDiscoveryStarted(serviceType: String?) {}
    override fun onDiscoveryStopped(serviceType: String?) {}
    override fun onServiceLost(serviceInfo: NsdServiceInfo?) {}

    override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
        Log.i("DiscoveryListener", "Found: ${serviceInfo?.toString()}")
        nsdManager.stopServiceDiscovery(this)
        nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
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
        })
    }

    private lateinit var nsdManager: NsdManager
    private var hostAddress: InetAddress? = null
    private var controller: Controller? = null
    private var mjpegPlayer: MjpegPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        setupUiListeners(cThread)
        controller = cThread
        val player = MjpegPlayer(address, PORT_CAMERA)
        player.setDisplay(surfaceHolder)
        player.start()
        mjpegPlayer = player
    }

    override fun onStop() {
        super.onStop()
        removeUiListeners()
        mjpegPlayer?.close()
        mjpegPlayer = null
        controller?.close()
        controller = null
        try {
            nsdManager.stopServiceDiscovery(this)
        } catch (exception: IllegalArgumentException) {
        }
    }

    private fun setupUiListeners(controller: Controller) {
        goForwardButton.setOnTouchListener(NavigationOnTouchListener(controller, "GF"))
        goBackwardButton.setOnTouchListener(NavigationOnTouchListener(controller, "GB"))
        turnRightButton.setOnTouchListener(NavigationOnTouchListener(controller, "TR"))
        turnLeftButton.setOnTouchListener(NavigationOnTouchListener(controller, "TL"))
        rotateClockwiseButton.setOnTouchListener(NavigationOnTouchListener(controller, "RC"))
        rotateAntiClockwiseButton.setOnTouchListener(NavigationOnTouchListener(controller, "RA"))
    }

    private fun removeUiListeners() {
        goForwardButton.setOnTouchListener(null)
        goBackwardButton.setOnTouchListener(null)
        turnRightButton.setOnTouchListener(null)
        turnLeftButton.setOnTouchListener(null)
        rotateClockwiseButton.setOnTouchListener(null)
        rotateAntiClockwiseButton.setOnTouchListener(null)
    }
}
