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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Settings(navController: NavController) {
    var videoLength by remember { mutableStateOf(30f) }  // Video length in seconds
    var videoQuality by remember { mutableStateOf("High") } // Video quality (Low, Medium, High)
    var crashSensitivity by remember { mutableStateOf(0.5f) }  // Crash detection sensitivity (0 to 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Settings")

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Video Length: ${videoLength.toInt()} seconds")
            Slider(
                value = videoLength,
                onValueChange = { videoLength = it },
                valueRange = 10f..60f
            )
        }

        // Video Quality Radio Buttons
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Video Quality")
            Row {
                RadioButton(
                    selected = videoQuality == "Low",
                    onClick = { videoQuality = "Low" }
                )
                Text(text = "Low", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = videoQuality == "Medium",
                    onClick = { videoQuality = "Medium" }
                )
                Text(text = "Medium", modifier = Modifier.padding(start = 8.dp))
            }
            Row {
                RadioButton(
                    selected = videoQuality == "High",
                    onClick = { videoQuality = "High" }
                )
                Text(text = "High", modifier = Modifier.padding(start = 8.dp))
            }
        }

        // Crash Detection Sensitivity Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Crash Detection Sensitivity: ${(crashSensitivity * 100).toInt()}%")
            Slider(
                value = crashSensitivity,
                onValueChange = { crashSensitivity = it },
                valueRange = 0f..1f
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = { /* Handle save action */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Save")
        }
    }
}