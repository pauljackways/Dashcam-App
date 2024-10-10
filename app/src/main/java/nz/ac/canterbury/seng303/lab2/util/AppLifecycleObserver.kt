package nz.ac.canterbury.seng303.lab2.util

import android.content.Context
import android.util.Log
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
            Lifecycle.Event.ON_STOP -> {
                if (recordingLogicViewModel.isRecording) {
                    NotificationHelper.sendRecordingNotification(context)
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                NotificationHelper.cancelRecordingNotification(context)
            }
            else -> {}
        }
    }
}