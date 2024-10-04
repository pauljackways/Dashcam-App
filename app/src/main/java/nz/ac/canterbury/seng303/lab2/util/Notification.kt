package nz.ac.canterbury.seng303.lab2.util

import android.Manifest
import android.app.Activity
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

object Notification {
    const val RECORDING_NOTIFICATION_ID = 1

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

        println("should send a notification now")

        // TODO replace with string constant thingy
        val name = "Actively Recording"
        val descriptionText = "Notifications to allow you to quickly navigate back to the app if it is currently recording"
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
            .setContentTitle("Your Dashcam is Recording")
            .setContentText("Tap here to open the app")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
//            .setOngoing(true)
        // TODO ongoing notifications requires a foreground service

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun cancelRecordingNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(RECORDING_NOTIFICATION_ID)
    }
}