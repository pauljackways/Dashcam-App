package nz.ac.canterbury.seng303.lab2.screens

import android.content.Intent
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel

@Composable
fun MainScreen(
    navController: NavController,
    recordingLogicViewModel: RecordingLogicViewModel = viewModel()
) {
    val context = LocalContext.current
    val isRecording = recordingLogicViewModel.isRecording

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    context.startActivity(intent)
                }
            ) {
                Text(text = "Open Gallery")
            }

            Button(
                onClick = { navController.navigate("settings") }
            ) {
                Text(text = "Settings")
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        onClick = { /* Capture frame action */ },
                        modifier = Modifier.size(100.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Capture")
                    }

                    Button(
                        onClick = { recordingLogicViewModel.stopAndSaveRecording() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Stop Recording")
                    }
                }
            } else {
                Button(
                    onClick = { recordingLogicViewModel.startRecording() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Start Recording")
                }
            }
        }
    }
}