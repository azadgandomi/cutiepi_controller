package com.sazadgankar.picontroller

import android.os.Message
import android.util.Log

class PowerChangeListener(private val controller: Controller) {

    companion object {
        const val TAG = "PowerChangeListener"
    }

    fun onPowerChange(power: Int) {
        Log.v(TAG, "power: $power")
        val message = Message.obtain()
        message.obj = ByteArray(2).apply {
            set(0, 'P'.toByte())
            set(1, power.toByte())
        }
        controller.handler?.sendMessage(message)
    }
}