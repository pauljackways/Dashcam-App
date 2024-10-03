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

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

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
            // Get the accelerometer values
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            // Check if the change exceeds the threshold
            if (Math.abs(x - lastX) > threshold || Math.abs(y - lastY) > threshold || Math.abs(z - lastZ) > threshold) {
                // Notify the listener
                listener.onAccelerationChanged(x, y, z)

                // Update the last reported values
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this case
    }

    interface AccelerometerListener {
        fun onAccelerationChanged(x: Float, y: Float, z: Float)
    }
}

