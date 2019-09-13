package com.sazadgankar.picontroller

import android.annotation.SuppressLint
import android.os.Message
import android.view.MotionEvent
import android.view.View
import java.util.*

class NavigationOnTouchListener(
    private val controller: Controller,
    private val command: String
) : View.OnTouchListener {

    companion object {
        const val TAG = "NavigationOnTouchListener"
        val commandHistory = Stack<String>()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val message = Message.obtain()
                message.obj = command.toByteArray(Charsets.US_ASCII)
                controller.handler?.sendMessage(message)
                commandHistory.push(command)
            }
            MotionEvent.ACTION_UP -> {
                commandHistory.removeAt(commandHistory.lastIndexOf(command))
                val message = Message.obtain()
                val cmd = if (commandHistory.empty()) STOP_MESSAGE else commandHistory.peek()
                message.obj = cmd.toByteArray(Charsets.US_ASCII)
                controller.handler?.sendMessage(message)
            }
        }
        return false
    }
}