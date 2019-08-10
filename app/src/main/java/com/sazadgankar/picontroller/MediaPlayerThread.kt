package com.sazadgankar.picontroller

import android.media.MediaPlayer
import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.Socket


private class MediaPlayerThread(private val mediaPlayer: MediaPlayer, private val address: String) : Thread() {

    private var socket: Socket? = null

    override fun run() {
        super.run()
        try {
            connect()
        } catch (exception: IOException) {
            Log.w("CommandThread", exception)

        }
    }

    private fun connect() {
        val address = InetAddress.getByName(address)
        Log.i("MediaPlayerThread", "Connecting...")
        socket = Socket(address, PORT_CAMERA)
        Log.i("MediaPlayerThread", "Connected!")
    }

    fun close() {
        socket?.close()
    }

    private class VideoDataSource
}