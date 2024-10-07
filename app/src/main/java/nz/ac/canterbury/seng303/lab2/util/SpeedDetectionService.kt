package nz.ac.canterbury.seng303.lab2.util

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class SpeedDetectionService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
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
}