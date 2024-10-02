package nz.ac.canterbury.seng303.lab2.viewmodels

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class CameraStateViewModel: ViewModel() {
    private var _isRecording by mutableStateOf(false)
    fun startRecording() {
        _isRecording = true
    }
    fun stopRecording() {
        _isRecording = false
    }
    fun isRecording(): Boolean {
        return _isRecording
    }
    private var _saveRequested by mutableStateOf(false)
    fun saveRecording() {
        _saveRequested = true
    }
    fun clearSaveRequest() {
        _saveRequested = false
    }
    fun saveRequested(): Boolean {
        return _saveRequested
    }
}