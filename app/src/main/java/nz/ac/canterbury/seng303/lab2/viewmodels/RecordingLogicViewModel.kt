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

    var intervalMillis: Long by mutableStateOf(30000)
        private set

    var toggle: Boolean by mutableStateOf(false)
        private set

    fun toggleRender() {
        toggle = !toggle
    }

    private var permit: Boolean by mutableStateOf(false)

    fun hasPermissions(): Boolean {
        return permit
    }

    fun setPermissions(perm: Boolean) {
        permit = perm
    }

    private var _recording: Recording? = null
        private set

    private var saveRequested = false

    private var autoSaveRequested = false

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

    var isRecording: Boolean by mutableStateOf(false)
        private set

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

    var audioEnable: Boolean by mutableStateOf(false)
        private set

    // didn't want to delete toggle just in case but using this for now
    fun updateAudioEnable(enable: Boolean) {
        audioEnable = enable
    }
    fun toggleAudioEnable() {
        audioEnable = !audioEnable
    }

}
