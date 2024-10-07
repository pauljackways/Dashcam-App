package nz.ac.canterbury.seng303.lab2.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import nz.ac.canterbury.seng303.lab2.util.AppLifecycleObserver
import nz.ac.canterbury.seng303.lab2.util.NotificationHelper
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel
import java.io.File
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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
    var previewView : PreviewView = remember { PreviewView(context) }
    val videoCapture : MutableState<VideoCapture<Recorder>?> = remember{ mutableStateOf(null) }

    LaunchedEffect(Unit) { // Use LaunchedEffect to run the coroutine on composition
        try {
            lifecycleOwner.lifecycleScope.launch {
                videoCapture.value = context.createVideoCaptureUseCase(
                    lifecycleOwner = lifecycleOwner,
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                    previewView = previewView
                )
            }
        } catch (e: Exception) {
            Log.e("Camera", "Error initializing camera: ${e.message}")
            // Handle the error, show a toast or update the UI accordingly
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = AppLifecycleObserver(context, recordingLogicViewModel)
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        InitCamera(context, previewView, cameraController, lifecycleOwner, recordingLogicViewModel) {
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
                        onClick = {
                            // Request notification permission (not required to start recording)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                NotificationHelper.requestPermissions(context as Activity)
                            }
                            startRecording(context, previewView, videoCapture, lifecycleOwner, cameraController, recordingLogicViewModel)
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
}

@Composable
fun InitCamera(
    context: Context,
    previewView: PreviewView,
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
            CameraPreview(context, previewView, cameraController, lifecycleOwner) {
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
fun CameraPreview(context: Context, previewView: PreviewView, cameraController: LifecycleCameraController, lifecycleOwner: LifecycleOwner, onCameraInitialized: () -> Unit) {
    if (!hasPermissions(context)) {
        return
    }

    cameraController.bindToLifecycle(lifecycleOwner)
    onCameraInitialized()

    AndroidView(
        factory = {previewView},
        modifier = Modifier.fillMaxSize()
    )
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

@SuppressLint("NewApi")
suspend fun Context.createVideoCaptureUseCase(
    lifecycleOwner: LifecycleOwner,
    cameraSelector: CameraSelector,
    previewView : PreviewView
) : VideoCapture<Recorder> {
    val preview = Preview.Builder()
        .build()
        .apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
    val qualitySelector = QualitySelector.from(
        Quality.FHD,
        FallbackStrategy.lowerQualityOrHigherThan(Quality.FHD)
    )

    val recorder = Recorder.Builder()
        .setExecutor(mainExecutor)
        .setQualitySelector(qualitySelector)
        .build()
    val videoCapture = VideoCapture.withOutput(recorder)

    val cameraProvider = getCameraProvider()
    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        videoCapture
    )
    return videoCapture
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

private fun handleFinalizeEvent(
    context: Context,
    previewView: PreviewView,
    videoCapture: MutableState<VideoCapture<Recorder>?>,
    lifecycleOwner: LifecycleOwner,
    cameraController: LifecycleCameraController,
    recordingLogicViewModel: RecordingLogicViewModel,
    finalizeEvent: VideoRecordEvent.Finalize
) {
    val uri = finalizeEvent.outputResults.outputUri
    Log.i("Camera", uri.toString())

    if (recordingLogicViewModel.saveRequested()) {
        if (uri != Uri.EMPTY) {
            val uriEncoded = URLEncoder.encode(
                uri.toString(),
                StandardCharsets.UTF_8.toString()
            )
        }
        startRecording(context, previewView, videoCapture, lifecycleOwner, cameraController, recordingLogicViewModel)
        recordingLogicViewModel.clearSaveRequest()
    } else {
        val file = File(uri.path!!)
        if (file.exists()) {
            if (file.delete()) {
                Log.d("FileDeletion", "File deleted successfully.")
            } else {
                Log.e("FileDeletion", "Failed to delete the file.")
            }
        } else {
            Log.e("FileDeletion", "File does not exist.")
        }
    }
}


@SuppressLint("MissingPermission", "NewApi")
fun startRecording(context: Context, previewView: PreviewView, videoCapture: MutableState<VideoCapture<Recorder>?>, lifecycleOwner: LifecycleOwner, cameraController: LifecycleCameraController, recordingLogicViewModel: RecordingLogicViewModel) {
    Log.d("Camera", "Start recording called")

    val outputFile = File(context.filesDir, convertTimestampToVideoTitle(System.currentTimeMillis()))

    videoCapture.value?.let { vidCap ->
        recordingLogicViewModel.setRecording(startRecordingVideo(
            context = context,
            videoCapture = vidCap,
            outputFile = outputFile,
            executor = context.mainExecutor,
            audioEnabled = recordingLogicViewModel.audioEnable
        ) { event ->
            when (event) {
                is VideoRecordEvent.Finalize -> handleFinalizeEvent(context, previewView, videoCapture, lifecycleOwner, cameraController, recordingLogicViewModel, event)
            }
        })
        recordingLogicViewModel.recordingStart = true
    }

    recordingLogicViewModel.startRecording()
}

fun saveRecording(recordingLogicViewModel: RecordingLogicViewModel) {
    Log.d("Camera", "Save recording called")
    recordingLogicViewModel.requestSave()
    stopRecording(recordingLogicViewModel)
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