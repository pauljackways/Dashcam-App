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
    val settings by viewModel.settings.collectAsState()

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
            Text(text = "Video Length: ${settings.videoLength} seconds")
            Slider(
                value = settings.videoLength.toFloat(),
                onValueChange = { newValue ->
                    coroutineScope.launch {
                        viewModel.saveSettings(
                            settings.copy(videoLength = newValue.toInt())
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
                    selected = settings.videoQuality == "Low",
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveSettings(
                                settings.copy(videoQuality = "Low")
                            )
                        }
                    }
                )
                Text(text = "Low", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = settings.videoQuality == "Medium",
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveSettings(
                                settings.copy(videoQuality = "Medium")
                            )
                        }
                    }
                )
                Text(text = "Medium", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = settings.videoQuality == "High",
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveSettings(
                                settings.copy(videoQuality = "High")
                            )
                        }
                    }
                )
                Text(text = "High", modifier = Modifier.padding(start = 8.dp))
            }
        }

        // Crash Detection Sensitivity Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Crash Detection Sensitivity: ${(settings.crashSensitivity * 100).toInt()}%")
            Slider(
                value = settings.crashSensitivity,
                onValueChange = { newValue ->
                    coroutineScope.launch {
                        viewModel.saveSettings(
                            settings.copy(crashSensitivity = newValue)
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
                        settings
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