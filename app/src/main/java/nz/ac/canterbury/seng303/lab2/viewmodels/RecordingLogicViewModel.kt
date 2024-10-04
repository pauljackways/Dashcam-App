package nz.ac.canterbury.seng303.lab2.viewmodels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import nz.ac.canterbury.seng303.lab2.util.Notification

class RecordingLogicViewModel : ViewModel() {
    var isRecording: Boolean by mutableStateOf(false)
        private set


//    TODO private var camera: Camera class
    private var videoSectionLengthMillis: Long? = null
    private var videoSectionSaveLocation: String? = null

    private val taskHandler = Handler(Looper.getMainLooper())
    private val startNextVideoSection = object : Runnable {
        override fun run() {
            val lengthMillisNullSafe = videoSectionLengthMillis
                ?: throw IllegalArgumentException("Video section length is null!")

            println("RUNNING!!!") // for testing

            // TODO Delete old video section (e.g., from 30 seconds ago) if it exists?
            stopAndSaveVideoSection()
            startVideoSection()

            taskHandler.postDelayed(this, lengthMillisNullSafe)

        }
    }


    fun startRecording(timePeriodMillis: Long, saveLocation: String) {
        // TODO handle more gracefully?
        if (isRecording) {
            throw IllegalStateException("Already recording!")
        }
        isRecording = true
        videoSectionLengthMillis = timePeriodMillis
        videoSectionSaveLocation = saveLocation

        taskHandler.post(startNextVideoSection)
    }

    fun stopAndSaveRecording() {
        stopAndSaveVideoSection()
        cancelRecording()

        // TODO stitch together videos and save to gallery (ffmpeg)
    }

    fun cancelRecording() {
        taskHandler.removeCallbacks(startNextVideoSection)
        isRecording = false
        // TODO camera.stopRecording()
        // TODO clear saved video sections?
    }


    private fun startVideoSection() {
        // camera.startRecording()
    }

    private fun stopAndSaveVideoSection() {
        val saveLocationNullSafe = videoSectionSaveLocation ?: throw IllegalArgumentException("Save location is null!")
        // TODO save to save location with date / time identifier
        // TODO camera.stopRecording(saveLocation, current time)
    }
}