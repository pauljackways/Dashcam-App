package nz.ac.canterbury.seng303.lab2.screens

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.lab2.util.AppLifecycleObserver
import nz.ac.canterbury.seng303.lab2.util.Notification
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel

@Composable
fun MainScreen(
    navController: NavController,
    recordingLogicViewModel: RecordingLogicViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = AppLifecycleObserver(context, recordingLogicViewModel)
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
            if (recordingLogicViewModel.isRecording) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        onClick = { recordingLogicViewModel.stopAndSaveRecording() },
                        modifier = Modifier.size(100.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Capture")
                    }

                    Button(
                        onClick = { recordingLogicViewModel.cancelRecording() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Stop Recording")
                    }
                }
            } else {
                Button(
                    // TODO update with actual save location, set time period from settings
                    onClick = {
                        recordingLogicViewModel.startRecording(
                            2 * 1000L,
                            "[replace with actual save location]",
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Notification.requestPermissions(context as Activity)
                        }
                    },
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