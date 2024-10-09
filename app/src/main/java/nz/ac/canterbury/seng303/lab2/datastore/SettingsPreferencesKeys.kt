package nz.ac.canterbury.seng303.lab2.datastore

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsPreferencesKeys {
    val VIDEO_LENGTH = intPreferencesKey("video_length")
    val CRASH_SENSITIVITY = floatPreferencesKey("crash_sensitivity")
    val AUTO_SAVE_INTERVAL = longPreferencesKey("auto_save_interval")
}