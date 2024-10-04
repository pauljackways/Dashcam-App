package nz.ac.canterbury.seng303.lab2.util

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel

class AppLifecycleObserver(
    private val context: Context,
    private val recordingLogicViewModel: RecordingLogicViewModel
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                if (recordingLogicViewModel.isRecording) {
                    Notification.sendRecordingNotification(context)
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                Notification.cancelRecordingNotification(context)
            }
            else -> {}
        }
    }
}