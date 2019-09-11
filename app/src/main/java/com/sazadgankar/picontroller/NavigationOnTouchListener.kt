package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View

class NavigationOnTouchListener(
    private val controller: Controller,
    private val command: String
) :
    View.OnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            Log.v("TOUCH", "Down: $command")
            val message = Message.obtain()
            message.obj = command
            controller.handler?.sendMessage(message)
        } else if (event.action == MotionEvent.ACTION_UP) {
            Log.v("TOUCH", "Up: $command")
            val message = Message.obtain()
            message.obj = "ST"
            controller.handler?.sendMessage(message)
        }
        return false
    }
}