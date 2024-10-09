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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.lab2.models.AppSettings
import nz.ac.canterbury.seng303.lab2.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController, viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()

    if (settings == null) {
        Text("Loading settings...")
        return
    }

    var tempVideoLength by rememberSaveable { mutableStateOf(settings!!.videoLength) }
    var tempCrashSensitivity by rememberSaveable { mutableStateOf(settings!!.crashSensitivity) }
    var tempAutoSaveIntervalMillis by rememberSaveable { mutableStateOf(settings!!.autoSaveIntervalMillis) }

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
                        Text(text = "Settings")
                        Spacer(modifier = Modifier.weight(1f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("Home") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
            // Video Length Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Video Length: $tempVideoLength seconds")
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Slider(
                        value = tempVideoLength.toFloat(),
                        onValueChange = { newValue ->
                            tempVideoLength = newValue.toInt()
                        },
                        valueRange = 10f..60f,
                        modifier = if (isLandscape) Modifier.fillMaxWidth(0.9f) else Modifier.fillMaxWidth()
                    )
                }
            }

            // Crash Detection Sensitivity Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Crash Detection Sensitivity: ${(tempCrashSensitivity * 100).toInt()}%")
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Slider(
                        value = tempCrashSensitivity,
                        onValueChange = { newValue ->
                            tempCrashSensitivity = newValue
                        },
                        valueRange = 0f..1f,
                        modifier = if (isLandscape) Modifier.fillMaxWidth(0.9f) else Modifier.fillMaxWidth()
                    )
                }
            }

            // Auto Save Interval Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Auto Save Interval: ${tempAutoSaveIntervalMillis / 1000} seconds")
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

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.saveSettings(
                            AppSettings(
                                videoLength = tempVideoLength,
                                crashSensitivity = tempCrashSensitivity,
                                autoSaveIntervalMillis = tempAutoSaveIntervalMillis
                            )
                        )
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Save")
            }
        }
    }
}