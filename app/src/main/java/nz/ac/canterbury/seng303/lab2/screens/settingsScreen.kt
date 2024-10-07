package nz.ac.canterbury.seng303.lab2.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.lab2.models.AppSettings
import nz.ac.canterbury.seng303.lab2.util.SpeedDetectionService
import nz.ac.canterbury.seng303.lab2.viewmodels.SettingsViewModel

@Composable
fun Settings(navController: NavController, viewModel: SettingsViewModel) {
    val context = LocalContext.current

    val settings by viewModel.settings.collectAsState()

    var tempVideoLength by remember { mutableStateOf(settings.videoLength) }
    var tempVideoQuality by remember { mutableStateOf(settings.videoQuality) }
    var tempCrashSensitivity by remember { mutableStateOf(settings.crashSensitivity) }

    var displayBackgroundLocationButton by remember { mutableStateOf(false) }

    // synchronizes values for UI -- weird fetching thing
    LaunchedEffect(settings) {
        tempVideoLength = settings.videoLength
        tempVideoQuality = settings.videoQuality
        tempCrashSensitivity = settings.crashSensitivity
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Settings")

        // Video Length Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Video Length: $tempVideoLength seconds")
            Slider(
                value = tempVideoLength.toFloat(),
                onValueChange = { newValue ->
                    tempVideoLength = newValue.toInt()
                },
                valueRange = 10f..60f
            )
        }

        // Video Quality Radio Buttons
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Video Quality")
            Row {
                RadioButton(
                    selected = tempVideoQuality == "Low",
                    onClick = { tempVideoQuality = "Low" }
                )
                Text(text = "Low", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = tempVideoQuality == "Medium",
                    onClick = { tempVideoQuality = "Medium" }
                )
                Text(text = "Medium", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = tempVideoQuality == "High",
                    onClick = { tempVideoQuality = "High" }
                )
                Text(text = "High", modifier = Modifier.padding(start = 8.dp))
            }
        }

        // Crash Detection Sensitivity Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Crash Detection Sensitivity: ${(tempCrashSensitivity * 100).toInt()}%")
            Slider(
                value = tempCrashSensitivity,
                onValueChange = { newValue ->
                    tempCrashSensitivity = newValue
                },
                valueRange = 0f..1f
            )
        }

        // Enable Driving Detection Button
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!SpeedDetectionService.hasPreciseLocationPermission(LocalContext.current)
                && !displayBackgroundLocationButton) {
                Text("To enable driving detection please allow additional permissions:")
                Button(onClick = {
                    SpeedDetectionService.requestPreciseLocationPermission(context)
                    displayBackgroundLocationButton = true
                }) {
                    Text("Allow permissions")
                }
            } else if (!SpeedDetectionService.hasBackgroundLocationPermission(LocalContext.current)
                && displayBackgroundLocationButton) {
                Text("To enable driving detection please allow background location permissions:")
                Button(onClick = {
                    SpeedDetectionService.requestBackgroundLocationPermission(context)
                    displayBackgroundLocationButton = false
                }) {
                    Text("Allow background location permissions")
                }
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
                            videoQuality = tempVideoQuality,
                            crashSensitivity = tempCrashSensitivity
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