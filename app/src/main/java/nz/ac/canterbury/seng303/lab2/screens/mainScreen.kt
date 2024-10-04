package nz.ac.canterbury.seng303.lab2.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.lab2.util.convertTimestampToVideoTitle
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel
import java.io.File
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.util.Consumer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun MainScreen(
    navController: NavController,
    recordingLogicViewModel: RecordingLogicViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    var isCameraInitialized by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        InitCamera(context, cameraController, lifecycleOwner, recordingLogicViewModel) {
            isCameraInitialized = true
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
                            onClick = {
                                saveRecording(recordingLogicViewModel)
                            },
                            modifier = Modifier.size(100.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(text = "Capture")
                        }

                        Button(
                            onClick = { stopRecording(recordingLogicViewModel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = "Stop Recording")
                        }
                    }
                } else if (isCameraInitialized && hasPermissions(context)) {
                    Button(
                        onClick = { startRecording(context, cameraController, recordingLogicViewModel) },
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
}

@Composable
fun InitCamera(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    recordingLogicViewModel: RecordingLogicViewModel,
    onCameraInitialized: () -> Unit
) {
    var hasPermissions by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasPermissions) {
            CameraPreview(context, cameraController, lifecycleOwner) {
                onCameraInitialized()
            }
        } else {
            NoCameraPermissions(context) {
                // Callback to update permissions
                hasPermissions = hasPermissions(context)
                recordingLogicViewModel.toggleRender()
            }
        }
    }
}

@SuppressLint("MissingPermission") // handled elsewhere
@Composable
fun CameraPreview(context: Context, cameraController: LifecycleCameraController, lifecycleOwner: LifecycleOwner, onCameraInitialized: () -> Unit) {
    if (!hasPermissions(context)) {
        return
    }
    cameraController.bindToLifecycle(lifecycleOwner)
    onCameraInitialized()

    AndroidView(factory = { innerContext ->
        PreviewView(innerContext).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_START
            controller = cameraController
        }
    })
}

@Composable
fun NoCameraPermissions(context: Context, onPermissionChange: () -> Unit) {
    val activity = context as? Activity
    Button(
        onClick = {
            activity?.let { requestPermissions(it) }
            onPermissionChange()
        }
    ) {
        Text(text = "Start camera")
    }
}


private const val requestCodePermissions = 1001
private val requiredPermissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
)

private fun hasPermissions(context: Context): Boolean {
    return requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

private fun requestPermissions(activity: Activity) {
    ActivityCompat.requestPermissions(activity, requiredPermissions, requestCodePermissions)
}




private fun handleFinalizeEvent(context: Context, cameraController: LifecycleCameraController, recordingLogicViewModel: RecordingLogicViewModel, finalizeEvent: VideoRecordEvent.Finalize) {
    val uri = finalizeEvent.outputResults.outputUri
    if (uri != Uri.EMPTY) {
        val contentResolver = context.contentResolver
        contentResolver.delete(uri, null, null)
    } else if (recordingLogicViewModel.saveRequested()) {
        // Save
        recordingLogicViewModel.clearSaveRequest()
        startRecording(context, cameraController, recordingLogicViewModel)
    }
    //recordingLogicViewModel.recording = null
}

@SuppressLint("MissingPermission", "NewApi")
fun startRecording(context: Context, cameraController: LifecycleCameraController, recordingLogicViewModel: RecordingLogicViewModel) {
    Log.d("Camera", "Start recording called")

    val outputFile = File(context.filesDir, convertTimestampToVideoTitle(System.currentTimeMillis()))

    recordingLogicViewModel.getVideoCapture()?.let { videoCapture ->
        recordingLogicViewModel.recordingStart = true
        recordingLogicViewModel.setRecording(startRecordingVideo(
            context = context,
            videoCapture = videoCapture,
            outputFile = outputFile,
            executor = context.mainExecutor,
            audioEnabled = recordingLogicViewModel.audioEnable
        ) { event ->
            when (event) {
                is VideoRecordEvent.Finalize -> handleFinalizeEvent(context, cameraController, recordingLogicViewModel, event)
            }
        })
    }

    recordingLogicViewModel.startRecording()
}

fun saveRecording(recordingLogicViewModel: RecordingLogicViewModel) {
    Log.d("Camera", "Save recording called")
    recordingLogicViewModel.requestSave()
    recordingLogicViewModel.stopRecording()
}

fun stopRecording(recordingLogicViewModel: RecordingLogicViewModel) {
    Log.d("Camera", "Stop recording called")
    recordingLogicViewModel.stopRecording()
}

@SuppressLint("NewApi")
suspend fun Context.getCameraProvider() : ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener(
            {
                continuation.resume(future.get())
            },
            mainExecutor
        )
    }
}

@SuppressLint("MissingPermission")
fun startRecordingVideo(
    context: Context,
    videoCapture: VideoCapture<Recorder>,
    outputFile: File,
    executor: Executor,
    audioEnabled: Boolean,
    consumer: Consumer<VideoRecordEvent>
): Recording {

    val outputOptions = FileOutputOptions.Builder(outputFile).build()

    return videoCapture.output
        .prepareRecording(context, outputOptions)
        .apply { if (audioEnabled) withAudioEnabled() }
        .start(executor, consumer)
}