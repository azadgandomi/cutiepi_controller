package com.sazadgankar.picontroller

import android.graphics.ImageDecoder
import android.util.Log
import android.view.SurfaceHolder
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer


class MjpegPlayer(private val surfaceHolder: SurfaceHolder, private val address: InetAddress, private val port: Int) :
    Thread(), AutoCloseable {
    companion object {
        const val TAG = "MjpegPlayer"

        const val JPEG_MARKER = 0xFF.toByte()
        const val JPEG_START_MARKER = 0xD8.toByte()
        const val JPEG_END_MARKER = 0xD9.toByte()
    }

    private var socket: DatagramSocket? = null


    private fun connect() {
        Log.i(TAG, "Connecting...")
        socket = DatagramSocket().also {
            it.connect(address, port)
        }
        Log.i(TAG, "Connected!")
    }

    override fun run() {
        super.run()
        try {
            connect()
            socket?.let {
                var isInFrame = false
                val bytesBuffer = ArrayList<Byte>()
                while (!interrupted()) {
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    it.receive(packet)
                    val iterator = buffer.take(packet.length).iterator()
                    for (b in iterator) {
                        if (isInFrame) {
                            bytesBuffer.add(b)
                        }
                        if (b == JPEG_MARKER) {
                            val nextByte = iterator.next()

                            when (nextByte) {
                                JPEG_START_MARKER -> {
                                    bytesBuffer.add(b)
                                    isInFrame = true
                                }

                                JPEG_END_MARKER -> {
                                    bytesBuffer.add(nextByte)
                                    // End of a jpeg, decode and show it

                                    val jpegBytes = ByteBuffer.wrap(bytesBuffer.toByteArray())
                                    try {
                                        val bitmap = ImageDecoder.decodeBitmap(
                                            ImageDecoder.createSource(jpegBytes)
                                        )
                                        val canvas = surfaceHolder.lockHardwareCanvas()
                                        canvas.drawBitmap(bitmap, null, surfaceHolder.surfaceFrame, null)
                                        surfaceHolder.unlockCanvasAndPost(canvas)
                                    } catch (exception: Exception) {
                                        Log.w(TAG, exception)
                                        if (isInterrupted) {
                                            return
                                        }
                                    }

                                    bytesBuffer.clear()
                                    isInFrame = false
                                }
                            }
                            if (isInFrame) {
                                bytesBuffer.add(nextByte)
                            }
                        }
                    }
                }
            }
        } catch (exception: IOException) {
            Log.w(TAG, exception)
        }
    }

    override fun close() {
        interrupt()
        socket?.close()
        socket = null
    }
}