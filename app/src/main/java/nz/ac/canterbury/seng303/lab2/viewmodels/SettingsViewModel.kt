package nz.ac.canterbury.seng303.lab2.viewmodels

import android.util.Log
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
import nz.ac.canterbury.seng303.lab2.models.AppSettings

class SettingsViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {

    val settings: StateFlow<AppSettings?> = dataStore.data
        .map { preferences ->
            AppSettings(
                videoLength = preferences[SettingsPreferencesKeys.VIDEO_LENGTH] ?: 30,
                crashSensitivity = preferences[SettingsPreferencesKeys.CRASH_SENSITIVITY] ?: 0.5f,
                autoSaveIntervalMillis = preferences[SettingsPreferencesKeys.AUTO_SAVE_INTERVAL] ?: 10000L,
                audioEnable = preferences[SettingsPreferencesKeys.AUDIO_ENABLE] ?: false
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun saveSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SettingsPreferencesKeys.VIDEO_LENGTH] = newSettings.videoLength
                preferences[SettingsPreferencesKeys.CRASH_SENSITIVITY] = newSettings.crashSensitivity
                preferences[SettingsPreferencesKeys.AUTO_SAVE_INTERVAL] = newSettings.autoSaveIntervalMillis
                preferences[SettingsPreferencesKeys.AUDIO_ENABLE] = newSettings.audioEnable
            }
            Log.d("SettingsViewModel", "Saved settings: $newSettings")
        }
    }
}
