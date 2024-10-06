package nz.ac.canterbury.seng303.lab2.util

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import nz.ac.canterbury.seng303.lab2.R

class SpeedDetectionService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
        // Create a notification to show the service is running
        val notification = NotificationCompat.Builder(this, Notification.DRIVING_DETECTION_FOREGROUND_CHANNEL_ID)
            .setContentTitle("Speed Detection")
            .setContentText("Monitoring your speed...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(Notification.DRIVING_DETECTION_FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMaxUpdateDelayMillis(2000)
            .setMinUpdateIntervalMillis(5000)
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
                val speed = location.speed // Speed in meters/second
                println("SPEED!!!!!!!!!!!!!: $speed")
                if (speed > 1) {
//                    sendSpeedNotification()
                }
            }
        }
    }

    private fun sendSpeedNotification() {
        val notification = NotificationCompat.Builder(this, Notification.DRIVING_DETECTION_FOREGROUND_CHANNEL_ID)
            .setContentTitle("Speed Alert")
            .setContentText("You are moving too fast!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(Notification.DRIVING_DETECTION_FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}