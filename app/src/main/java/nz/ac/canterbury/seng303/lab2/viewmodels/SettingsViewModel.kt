package nz.ac.canterbury.seng303.lab2.viewmodels

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.lab2.datastore.SettingsPreferencesKeys

class SettingsViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {

    val videoLength: StateFlow<Int> = dataStore.data
        .map { preferences ->
            preferences[SettingsPreferencesKeys.VIDEO_LENGTH] ?: 30
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 30)

    val videoQuality: StateFlow<String> = dataStore.data
        .map { preferences ->
            preferences[SettingsPreferencesKeys.VIDEO_QUALITY] ?: "High"
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, "High")

    val crashSensitivity: StateFlow<Float> = dataStore.data
        .map { preferences ->
            preferences[SettingsPreferencesKeys.CRASH_SENSITIVITY] ?: 0.5f
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.5f)

    fun saveSettings(videoLength: Int, videoQuality: String, crashSensitivity: Float) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SettingsPreferencesKeys.VIDEO_LENGTH] = videoLength
                preferences[SettingsPreferencesKeys.VIDEO_QUALITY] = videoQuality
                preferences[SettingsPreferencesKeys.CRASH_SENSITIVITY] = crashSensitivity
            }
        }
    }
}