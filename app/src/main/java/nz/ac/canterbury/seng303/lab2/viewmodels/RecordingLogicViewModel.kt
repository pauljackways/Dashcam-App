package nz.ac.canterbury.seng303.lab2.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel

class RecordingLogicViewModel : ViewModel() {
    var isRecording = false
        private set


//    TODO private var camera: Camera class
    private var videoSectionLengthMillis: Long? = null
    private var videoSectionSaveLocation: String? = null

    private val taskHandler = Handler(Looper.getMainLooper())
    private val startNextVideoSection = object : Runnable {
        override fun run() {
            val lengthMillisNullSafe = videoSectionLengthMillis
                ?: throw IllegalArgumentException("Video section length is null!")
            val saveLocationNullSafe = videoSectionSaveLocation
                ?: throw IllegalArgumentException("Save location is null!")

            println("RUNNING!!!") // for testing

            // TODO Delete old video section (e.g., from 30 seconds ago) if it exists?
            stopAndSaveVideoSection(saveLocationNullSafe)
            startVideoSection()

            taskHandler.postDelayed(this, lengthMillisNullSafe)

        }
    }


    fun startRecording(timePeriodMillis: Long, saveLocation: String) {
        isRecording = true
        videoSectionLengthMillis = timePeriodMillis
        videoSectionSaveLocation = saveLocation

        taskHandler.post(startNextVideoSection)
    }

    fun stopAndSaveRecording(saveLocation: String /* might not be string */) {
        // Stop task from running periodically
        taskHandler.removeCallbacks(startNextVideoSection)
        stopAndSaveVideoSection(saveLocation)

        // TODO stitch together videos and save to gallery (ffmpeg)
    }

    fun cancelRecording() {
        // Stop task from running periodically
        taskHandler.removeCallbacks(startNextVideoSection)
        // TODO camera.stopRecording()
        // TODO clear saved video sections?
    }


    private fun startVideoSection() {
        // camera.startRecording()
    }

    private fun stopAndSaveVideoSection(saveLocation: String /* might not be string */) {
        // TODO save to save location with date / time identifier
        // TODO camera.stopRecording(saveLocation, current time)
    }
}