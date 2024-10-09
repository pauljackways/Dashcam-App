package nz.ac.canterbury.seng303.lab2.viewmodels

import androidx.camera.core.CameraSelector
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.Recorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RecordingLogicViewModel : ViewModel() {

    var intervalMillis: Long by mutableStateOf(10000) // 10 seconds
        private set

    var toggle: Boolean by mutableStateOf(false)
        private set

    fun toggleRender() {
        toggle = !toggle
    }
    // Recording-related properties
    private var _recording: Recording? = null
        private set

    private var saveRequested = false

    private var autoSaveRequested = false


    // Getter and Setter for recording
    fun getRecording(): Recording? {
        return _recording
    }

    fun setRecording(newRecording: Recording?) {
        _recording = newRecording
    }

    fun saveRequested(): Boolean {
        return saveRequested
    }

    fun autoSaveRequested(): Boolean {
        return autoSaveRequested
    }

    fun requestSave() {
        saveRequested = true
    }

    fun clearSaveRequest() {
        saveRequested = false
    }

    fun clearAutoSaveRequest() {
        autoSaveRequested = false
    }

    // Mutable state for recording status
    var isRecording: Boolean by mutableStateOf(false)
        private set

    // Recording start state
    var recordingStart: Boolean by mutableStateOf(false)

    fun startRecording() {
        recordingStart = true
        isRecording = true
    }

    fun stopRecording() {
        _recording?.stop()
        recordingStart = false
        isRecording = false
    }

    fun autoSave() {
        autoSaveRequested = true
        _recording?.stop()
    }

    // Audio enable state
    var audioEnable: Boolean by mutableStateOf(false)
        private set

    fun toggleAudioEnable() {
        audioEnable = !audioEnable
    }

}
