package nz.ac.canterbury.seng303.lab2.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class Accelerometer(context: Context, private val listener: AccelerometerListener) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val threshold = 0.5f
    private val crashThreshold = -5.0f // Threshold for crash detection (adjust as needed)

    // Store the last reported acceleration values
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    // Define a time interval for reporting in milliseconds
    private val reportingInterval = 100L
    private var lastReportedTime = System.currentTimeMillis()

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {

            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            detectCrash(x, y, z)

            val currentTime = System.currentTimeMillis()
            if ((Math.abs(x - lastX) > threshold || Math.abs(y - lastY) > threshold || Math.abs(z - lastZ) > threshold) &&
                (currentTime - lastReportedTime >= reportingInterval)) {

                listener.onAccelerationChanged(x, y, z)

                lastX = x
                lastY = y
                lastZ = z
                lastReportedTime = currentTime
            }
        }
    }

    private fun detectCrash(x: Float, y: Float, z: Float) {
        val accelerationMagnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (accelerationMagnitude < crashThreshold) {
            listener.onCrashDetected()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    interface AccelerometerListener {
        fun onAccelerationChanged(x: Float, y: Float, z: Float)
        fun onCrashDetected() // New method for crash detection
    }
}