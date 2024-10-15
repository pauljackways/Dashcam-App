package nz.ac.canterbury.seng303.lab2.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.lab2.R
import nz.ac.canterbury.seng303.lab2.models.AppSettings
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel
import nz.ac.canterbury.seng303.lab2.util.SpeedDetectionService
import nz.ac.canterbury.seng303.lab2.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController) {
    val context = LocalContext.current

    val settingsViewModel: SettingsViewModel = koinViewModel()
    val recordingLogicViewModel: RecordingLogicViewModel = koinViewModel()

    val settings by settingsViewModel.settings.collectAsState()

    var displayBackgroundLocationButton: Boolean? by remember { mutableStateOf(null) }

    if (settings == null) {
        Text(text = stringResource(R.string.loading_settings))
        return
    }

    var tempVideoLength by rememberSaveable { mutableStateOf(settings!!.videoLength) }
    var tempCrashSensitivity by rememberSaveable { mutableStateOf(settings!!.crashSensitivity) }
    var tempAutoSaveIntervalMillis by rememberSaveable { mutableStateOf(settings!!.autoSaveIntervalMillis) }
    var tempAudioEnable by rememberSaveable { mutableStateOf(settings!!.audioEnable) }

    val coroutineScope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLandscape) Spacer(modifier = Modifier.weight(0.85f))
                        else Spacer(modifier = Modifier.weight(0.75f))
                        Text(text = (stringResource(R.string.settings_title)))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("Home") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        val modifier = if (isLandscape) {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        } else {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val sensitivityRange = 15f..40f
            Column(modifier = Modifier.fillMaxWidth()) {
                val sensitivityPercentage = ((tempCrashSensitivity - sensitivityRange.start) / (sensitivityRange.endInclusive - sensitivityRange.start)) * 100

                Text(
                    text = stringResource(R.string.crash_detection_sensitivity) + ": ${sensitivityPercentage.toInt()}%"
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Slider(
                        value = tempCrashSensitivity,
                        onValueChange = { newValue ->
                            tempCrashSensitivity = newValue
                        },
                        valueRange = sensitivityRange,
                        modifier = if (isLandscape) Modifier.fillMaxWidth(0.9f) else Modifier.fillMaxWidth()
                    )
                }
            }

            // Auto Save Interval Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.auto_save_interval) + ": ${tempAutoSaveIntervalMillis / 1000} "
                        + stringResource(R.string.seconds))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Slider(
                        value = tempAutoSaveIntervalMillis.toFloat(),
                        onValueChange = { newValue ->
                            tempAutoSaveIntervalMillis = newValue.toLong()
                        },
                        valueRange = 10000f..60000f, // Between 10s and 60s
                        modifier = if (isLandscape) Modifier.fillMaxWidth(0.9f) else Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.audio_enable))
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = tempAudioEnable,
                    onCheckedChange = { tempAudioEnable = it },
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            // Enable Driving Detection Button
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!SpeedDetectionService.hasPreciseLocationPermission(LocalContext.current)
                    && displayBackgroundLocationButton == null) {
                    Text("To enable driving detection please allow additional permissions:")
                    Button(onClick = {
                        SpeedDetectionService.requestPreciseLocationPermission(context)
                        displayBackgroundLocationButton = true
                    }) {
                        Text("Allow permissions")
                    }
                } else if (!SpeedDetectionService.hasBackgroundLocationPermission(LocalContext.current)
                    && displayBackgroundLocationButton == null || displayBackgroundLocationButton == true) {
                    Text("To enable driving detection please allow background location permissions:")
                    Button(onClick = {
                        SpeedDetectionService.requestBackgroundLocationPermission(context)
                        displayBackgroundLocationButton = false
                    }) {
                        Text("Allow all the time")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        settingsViewModel.saveSettings(
                            AppSettings(
                                videoLength = tempVideoLength,
                                crashSensitivity = tempCrashSensitivity,
                                autoSaveIntervalMillis = tempAutoSaveIntervalMillis,
                                audioEnable = tempAudioEnable
                            )
                        )

                        recordingLogicViewModel.updateAudioEnable(tempAudioEnable)
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.save_button))
            }
        }
    }
}