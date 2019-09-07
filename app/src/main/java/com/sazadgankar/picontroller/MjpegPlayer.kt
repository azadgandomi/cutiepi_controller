package com.sazadgankar.picontroller

import android.graphics.ImageDecoder
import android.util.Log
import android.view.SurfaceHolder
import java.io.BufferedInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer


class MjpegPlayer :
    Thread(), AutoCloseable {
    companion object {
        const val TAG = "MjpegPlayer"

        const val JPEG_MARKER = 0xFF.toByte()
        const val JPEG_START_MARKER = 0xD8.toByte()
        const val JPEG_END_MARKER = 0xD9.toByte()
    }


    @Volatile
    private var surfaceHolder: SurfaceHolder? = null
    private var address: InetAddress? = null
    private var stringAddress: String? = null
    private var port: Int = 0

    @Volatile
    private var inputStream: BufferedInputStream? = null

    fun setDisplay(holder: SurfaceHolder?) {
        surfaceHolder = holder
    }

    private fun connect() {
        if (address == null) {
            address = InetAddress.getByName(stringAddress)
        }
        Log.i(TAG, "Connecting...")
        val socket = Socket(address, port)
        Log.i(TAG, "Connected!")
        inputStream = socket.getInputStream().buffered()
    }

    fun start(address: InetAddress, port: Int) {
        this.address = address
        this.port = port
        super.start()
    }

    fun start(address: String, port: Int) {
        this.stringAddress = address
        this.port = port
        super.start()
    }

    override fun run() {
        super.run()
        try {
            connect()
            inputStream?.let {

                var isInFrame = false
                val iterator = it.iterator()
                val bytesBuffer = ArrayList<Byte>()
                for (b in iterator) {
                    if (isInFrame) {
                        bytesBuffer.add(b)
                    }
                    if (b == JPEG_MARKER) {
                        val nextByte = iterator.nextByte()

                        when (nextByte) {
                            JPEG_START_MARKER -> {
                                bytesBuffer.add(b)
                                isInFrame = true
                            }

                            JPEG_END_MARKER -> {
                                bytesBuffer.add(nextByte)
                                // End of a jpeg, decode and show it
                                surfaceHolder?.let { holder ->
                                    val jpegBytes = ByteBuffer.wrap(bytesBuffer.toByteArray())
                                    try {
                                        val bitmap = ImageDecoder.decodeBitmap(
                                            ImageDecoder.createSource(jpegBytes)
                                        )
                                        val canvas = holder.lockHardwareCanvas()
                                        canvas.drawBitmap(bitmap, null, holder.surfaceFrame, null)
                                        holder.unlockCanvasAndPost(canvas)
                                    } catch (exception: Exception) {
                                        Log.w(TAG, exception)
                                        if (isInterrupted) {
                                            return
                                        }
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

        } catch (exception: IOException) {

            Log.w(TAG, exception)
        }
    }

    override fun close() {
        interrupt()
        inputStream?.close()
        inputStream = null
    }
}