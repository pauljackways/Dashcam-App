package nz.ac.canterbury.seng303.lab2.util

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class SpeedDetectionService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("STARTED!!!!")
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
        println("STARTED HERE!!!!!")
        NotificationHelper.createDrivingDetectionServiceNotificationChannel(this)
        val notification = NotificationHelper.getDrivingDetectionServiceNotification(this)
        startForeground(NotificationHelper.DRIVING_DETECTION_FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMaxUpdateDelayMillis(2000)
            .setIntervalMillis(30000)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val speedKPH = location.speed * 18/5
                if (speedKPH > 30) {
                    NotificationHelper.sendDrivingNotification(this@SpeedDetectionService, speedKPH.toString())
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val PRECISE_LOCATION_REQUEST_CODE = 1235
        const val BACKGROUND_LOCATION_REQUEST_CODE = 1236

        fun hasPreciseLocationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun hasBackgroundLocationPermission(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                return true
            }
            return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        }

        fun requestPreciseLocationPermission(context: Context) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            ActivityCompat.requestPermissions(
                context as Activity,
                permissions,
                PRECISE_LOCATION_REQUEST_CODE
            )
        }

        fun requestBackgroundLocationPermission(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                return

            val permissions = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            ActivityCompat.requestPermissions(
                context as Activity,
                permissions,
                BACKGROUND_LOCATION_REQUEST_CODE
            )
        }

        fun start(context: Context) {
            println("Permission: ${hasPreciseLocationPermission(context)}")
            if (!hasPreciseLocationPermission(context)) {
                return
            }
            val serviceIntent = Intent(context, SpeedDetectionService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}