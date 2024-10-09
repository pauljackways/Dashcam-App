package nz.ac.canterbury.seng303.lab2.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel
import nz.ac.canterbury.seng303.lab2.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val Context.settingsDataStore by preferencesDataStore("settings")

val appModule = module {
    single { get<Context>().settingsDataStore}
    viewModel { RecordingLogicViewModel() }
    viewModel { SettingsViewModel(get())}
}