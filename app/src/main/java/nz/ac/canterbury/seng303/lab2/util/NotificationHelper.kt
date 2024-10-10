package nz.ac.canterbury.seng303.lab2.util

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import nz.ac.canterbury.seng303.lab2.MainActivity

object NotificationHelper {
    private const val RECORDING_NOTIFICATION_ID = 1
    private const val DRIVING_NOTIFICATION_ID = 2

    const val DRIVING_DETECTION_FOREGROUND_NOTIFICATION_ID = 3
    private const val DRIVING_DETECTION_FOREGROUND_CHANNEL_ID = "driving-detection-foreground-channel-id"

    private const val REQUEST_CODE_PERMISSIONS = 30369

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasPermissions(context: Context): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, REQUEST_CODE_PERMISSIONS)
    }


    fun sendRecordingNotification(context: Context) {
        val channelId = "active-recording-channel-id"
        val notificationId = RECORDING_NOTIFICATION_ID

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(context as Activity)
            } else {
                return
            }
        }

        // TODO replace with string constant thingy
        val name = "Dashcam not recording warning"
        val descriptionText = "Notifies you when you've closed the app while recording."
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Warning, Dashcam recording stopped!")
            .setContentText("Your dashcam only records while the app is open, tap here to reopen.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun cancelRecordingNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(RECORDING_NOTIFICATION_ID)
    }

    fun sendDrivingNotification(context: Context, speed: String) {
        val channelId = "driving-detection-channel-id"
        val notificationId = DRIVING_NOTIFICATION_ID

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // TODO replace with string constant thingy
        val name = "Driving Detected"
        val descriptionText = "Notifications that display when the app detects you may be driving"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Are you driving?")
            .setContentText("We detected you may be driving without the dashcam. Tap here to open the app.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun cancelDrivingNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(DRIVING_NOTIFICATION_ID)
    }

    fun createDrivingDetectionServiceNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            DRIVING_DETECTION_FOREGROUND_CHANNEL_ID,
            "Speed Detection Service Channel",
            NotificationManager.IMPORTANCE_MIN
        )
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun getDrivingDetectionServiceNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, DRIVING_DETECTION_FOREGROUND_CHANNEL_ID)
            .setContentTitle("Dashcam Speed Detection")
            .setContentText("Monitoring your speed...")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}