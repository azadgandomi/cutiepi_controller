package com.sazadgankar.picontroller

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.InetAddress


class LocalNetworkConnectionActivity : AppCompatActivity(),
    NsdManager.DiscoveryListener {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    override fun onStart() {
        super.onStart()
        hostAddress.let { address ->
            if (address != null) {
                connect(address)
            } else {
                nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this)
            }
        }
    }

    private fun connect(address: InetAddress) {
        val cThread = Controller(address, surfaceView.holder, powerBar)
        cThread.start()
        setupUiListeners(cThread)
        controller = cThread
    }

    override fun onStop() {
        super.onStop()
        removeUiListeners()
        controller?.close()
        controller = null
        try {
            nsdManager.stopServiceDiscovery(this)
        } catch (exception: IllegalArgumentException) {
        }
    }

    private fun setupUiListeners(controller: Controller) {
        goForwardButton.setOnTouchListener(NavigationOnTouchListener(controller, GO_FORWARD_MESSAGE))
        goBackwardButton.setOnTouchListener(NavigationOnTouchListener(controller, GO_BACKWARD_MESSAGE))
        turnRightButton.setOnTouchListener(NavigationOnTouchListener(controller, TURN_RIGHT_MESSAGE))
        turnLeftButton.setOnTouchListener(NavigationOnTouchListener(controller, TURN_LEFT_MESSAGE))
        rotateClockwiseButton.setOnTouchListener(NavigationOnTouchListener(controller, ROTATE_CLOCKWISE_MESSAGE))
        rotateAntiClockwiseButton.setOnTouchListener(NavigationOnTouchListener(controller, ROTATE_ANTI_CLOCKWISE_MESSAGE))
        powerBar.powerChangeListener = PowerChangeListener(controller)
    }

    private fun removeUiListeners() {
        goForwardButton.setOnTouchListener(null)
        goBackwardButton.setOnTouchListener(null)
        turnRightButton.setOnTouchListener(null)
        turnLeftButton.setOnTouchListener(null)
        rotateClockwiseButton.setOnTouchListener(null)
        rotateAntiClockwiseButton.setOnTouchListener(null)
        powerBar.powerChangeListener = null
    }
}
