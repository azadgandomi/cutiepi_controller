package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.content.Context
import android.net.nsd.NsdManager
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class InternetConnectionActivity : AppCompatActivity(), SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.w("SurfaceHolder", "Surface Changed!")
        mjpegPlayer.setDisplay(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.i("SurfaceHolder", "Surface Destroyed!")
        mjpegPlayer.setDisplay(null)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.i("SurfaceHolder", "Surface Created!")
    }

    private lateinit var nsdManager: NsdManager
    private var commandThread: Controller? = null
    private var mjpegPlayer = MjpegPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
        surfaceView.holder.addCallback(this)
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    override fun onStart() {
        super.onStart()

        commandThread = Controller(INTERNET_ADDRESS)
        commandThread?.start()
        mjpegPlayer.start(INTERNET_ADDRESS, PORT_CAMERA)
    }

    override fun onStop() {
        super.onStop()
        mjpegPlayer.close()
        commandThread?.close()
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
                message.obj = "ST"
                commandThread?.handler?.sendMessage(message)
            }
            return false
        }
    }
}
