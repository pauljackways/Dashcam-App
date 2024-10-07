package nz.ac.canterbury.seng303.lab2.datastore

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsPreferencesKeys {
    val VIDEO_LENGTH = intPreferencesKey("video_length")
    val VIDEO_QUALITY = stringPreferencesKey("video_quality")
    val CRASH_SENSITIVITY = floatPreferencesKey("crash_sensitivity")
}