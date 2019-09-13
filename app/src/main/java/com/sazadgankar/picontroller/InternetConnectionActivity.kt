package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.InetAddress

class InternetConnectionActivity : AppCompatActivity(), SurfaceHolder.Callback {
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

    private var addressResolverTask: AddressResolverTask? = null
    private var hostAddress: InetAddress? = null
    private var controller: Controller? = null
    private var mjpegPlayer: MjpegPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView.holder.addCallback(this)
    }

    override fun onStart() {
        super.onStart()
        hostAddress.let { address ->
            if (address != null) {
                connect(address)
            } else {
                val resolverTask = AddressResolverTask()
                resolverTask.execute(HOST_NAME)
                addressResolverTask = resolverTask
            }
        }
    }

    private fun connect(address: InetAddress) {
        val cThread = Controller(address, powerBar)
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
        addressResolverTask?.cancel(true)
        addressResolverTask = null
    }

    private fun setupUiListeners(controller: Controller) {
        goForwardButton.setOnTouchListener(
            NavigationOnTouchListener(
                controller,
                GO_FORWARD_MESSAGE
            )
        )
        goBackwardButton.setOnTouchListener(
            NavigationOnTouchListener(
                controller,
                GO_BACKWARD_MESSAGE
            )
        )
        turnRightButton.setOnTouchListener(
            NavigationOnTouchListener(
                controller,
                TURN_RIGHT_MESSAGE
            )
        )
        turnLeftButton.setOnTouchListener(NavigationOnTouchListener(controller, TURN_LEFT_MESSAGE))
        rotateClockwiseButton.setOnTouchListener(
            NavigationOnTouchListener(
                controller,
                ROTATE_CLOCKWISE_MESSAGE
            )
        )
        rotateAntiClockwiseButton.setOnTouchListener(
            NavigationOnTouchListener(
                controller,
                ROTATE_ANTI_CLOCKWISE_MESSAGE
            )
        )
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

    @SuppressLint("StaticFieldLeak")
    private inner class AddressResolverTask : AsyncTask<String, Void, InetAddress>() {
        override fun doInBackground(vararg params: String?): InetAddress {
            return InetAddress.getByName(params[0])
        }

        override fun onPostExecute(result: InetAddress?) {
            connect(result!!)
        }
    }

}
