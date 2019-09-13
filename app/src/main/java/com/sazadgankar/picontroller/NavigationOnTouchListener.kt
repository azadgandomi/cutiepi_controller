package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View

class NavigationOnTouchListener(
    private val controller: Controller,
    private val command: String
) : View.OnTouchListener {

    companion object {
        const val TAG = "NavigationOnTouchListener"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            Log.v(TAG, "Touch Down: $command")
            val message = Message.obtain()
            message.obj = command.toByteArray(Charsets.US_ASCII)
            controller.handler?.sendMessage(message)
        } else if (event.action == MotionEvent.ACTION_UP) {
            Log.v(TAG, "Touch Up: $command")
            val message = Message.obtain()
            message.obj = "ST".toByteArray(Charsets.US_ASCII)
            controller.handler?.sendMessage(message)
        }
        return false
    }
}