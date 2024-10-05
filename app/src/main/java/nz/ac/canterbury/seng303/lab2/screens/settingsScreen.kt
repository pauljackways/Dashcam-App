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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.lab2.viewmodels.SettingsViewModel

@Composable
fun Settings(navController: NavController, viewModel: SettingsViewModel) {
    // Collect state from the view model
    val videoLength by viewModel.videoLength.collectAsState()
    val videoQuality by viewModel.videoQuality.collectAsState()
    val crashSensitivity by viewModel.crashSensitivity.collectAsState()

    // Remember coroutine scope to save settings on button click
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
            Text(text = "Video Length: $videoLength seconds")
            Slider(
                value = videoLength.toFloat(),  // Directly use the value from the ViewModel
                onValueChange = { newValue ->
                    coroutineScope.launch {
                        viewModel.saveSettings(
                            videoLength = newValue.toInt(),
                            videoQuality = videoQuality,
                            crashSensitivity = crashSensitivity
                        )
                    }
                },
                valueRange = 10f..60f
            )
        }

        // Video Quality Radio Buttons
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Video Quality")
            Row {
                RadioButton(
                    selected = videoQuality == "Low",
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveSettings(
                                videoLength = videoLength,
                                videoQuality = "Low",
                                crashSensitivity = crashSensitivity
                            )
                        }
                    }
                )
                Text(text = "Low", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = videoQuality == "Medium",
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveSettings(
                                videoLength = videoLength,
                                videoQuality = "Medium",
                                crashSensitivity = crashSensitivity
                            )
                        }
                    }
                )
                Text(text = "Medium", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = videoQuality == "High",
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveSettings(
                                videoLength = videoLength,
                                videoQuality = "High",
                                crashSensitivity = crashSensitivity
                            )
                        }
                    }
                )
                Text(text = "High", modifier = Modifier.padding(start = 8.dp))
            }
        }

        // Crash Detection Sensitivity Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Crash Detection Sensitivity: ${(crashSensitivity * 100).toInt()}%")
            Slider(
                value = crashSensitivity,
                onValueChange = { newValue ->
                    coroutineScope.launch {
                        viewModel.saveSettings(
                            videoLength = videoLength,
                            videoQuality = videoQuality,
                            crashSensitivity = newValue
                        )
                    }
                },
                valueRange = 0f..1f
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.saveSettings(
                        videoLength,
                        videoQuality,
                        crashSensitivity
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