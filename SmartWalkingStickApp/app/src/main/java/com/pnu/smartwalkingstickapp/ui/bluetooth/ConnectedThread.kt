package com.pnu.smartwalkingstickapp.ui.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class ConnectedThread(private val mmSocket: BluetoothSocket, private val mHandler: Handler) :
    Thread() {
    private val TAG = "jiwoo"
    private val mmInStream: InputStream = mmSocket.inputStream
    private val mmOutStream: OutputStream = mmSocket.outputStream
    private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

    override fun run() {
        val buffer = ByteArray(1024) // buffer store for the stream
        var bytes: Int // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.available()
                if (bytes != 0) {
                    SystemClock.sleep(100) //pause and wait for rest of data. Adjust this depending on your sending speed.
                    bytes = mmInStream.available() // how many bytes are ready to be read?
                    bytes =
                        mmInStream.read(buffer, 0, bytes) // record how many bytes we actually read
                    mHandler.obtainMessage(2, bytes, -1, buffer)
                        .sendToTarget() // Send the obtained bytes to the UI activity
                }
            } catch (e: IOException) {
                e.printStackTrace()
                break
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    fun write(bytes: ByteArray) {
        try {
            mmOutStream.write(bytes)
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)

        }

        // Share the sent message with the UI activity.

    }

    // Call this method from the main activity to shut down the connection.
    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }
}