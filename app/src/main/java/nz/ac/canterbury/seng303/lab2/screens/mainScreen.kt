package nz.ac.canterbury.seng303.lab2.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.lab2.util.VideoHelper
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.res.stringResource
import nz.ac.canterbury.seng303.lab2.R
import org.koin.androidx.compose.koinViewModel
import nz.ac.canterbury.seng303.lab2.util.Accelerometer
import nz.ac.canterbury.seng303.lab2.viewmodels.SettingsViewModel


@Composable
fun MainScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    var isCameraInitialized by remember { mutableStateOf(false) }
    var previewView : PreviewView = remember { PreviewView(context) }
    val videoCapture : MutableState<VideoCapture<Recorder>?> = remember{ mutableStateOf(null) }
    val recordingLogicViewModel: RecordingLogicViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val crashCooldownMillis = 5000
    var lastCrashDetectionMillis = System.currentTimeMillis()

    val infiniteTransition = rememberInfiniteTransition(label = "Infinite recording button rotation")
    val buttonRotationAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing), // 5 seconds per rotation
            repeatMode = RepeatMode.Restart
        ), label = "Infinite recording button rotation"
    )

    LaunchedEffect(Unit) { // Use LaunchedEffect to run the coroutine on composition
        val accelerometerListener = object : Accelerometer.AccelerometerListener {
            override fun onAccelerationChanged(x: Float, y: Float, z: Float) {
//                Log.d("Accelerometer", "Acceleration changed: x = $x, y = $y, z = $z")
            }

            override fun onCrashDetected() {
                if (System.currentTimeMillis() - lastCrashDetectionMillis > crashCooldownMillis) {
                    lastCrashDetectionMillis = System.currentTimeMillis()
                    saveRecording(recordingLogicViewModel)
                }
            }
        }

        val accelerometer = Accelerometer(context, accelerometerListener, settingsViewModel)
        accelerometer.start()

        VideoHelper.deleteAllVideosInFolder(context.filesDir, "mp4")
        recordingLogicViewModel.setPermissions(hasPermissions(context))
        recordingLogicViewModel.toggleRender()
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

    InitCamera(context, previewView, cameraController, lifecycleOwner, recordingLogicViewModel) {
        isCameraInitialized = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { navController.navigate("settings") },
            modifier = Modifier
                .align(if (isPortrait) Alignment.TopStart else Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.settings_button))
        }
        Button(
            onClick = {
                navController.navigate("Gallery")
            },
            modifier = Modifier
                .align(if (isPortrait) Alignment.TopEnd else Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.open_gallery_button))
        }

        if (isCameraInitialized && hasPermissions(context)) {
            Box(
                modifier = Modifier
                    .align(if (isPortrait) Alignment.BottomCenter else Alignment.CenterEnd)
                    .padding(vertical = 16.dp, horizontal = (21 + 16 + 14).dp)
                    .offset(y = 21.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column {
                        Box() {
                            fun buttonClicked() {
                                if (recordingLogicViewModel.isRecording) {
                                    stopRecording(recordingLogicViewModel)
                                } else {
                                    // Request notification permission (not required to start recording)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        NotificationHelper.requestPermissions(context as Activity)
                                    }
                                    startRecording(
                                        context, previewView, videoCapture, lifecycleOwner,
                                        cameraController, recordingLogicViewModel
                                    )
                                }
                            }
                            Button(
                                onClick = { buttonClicked() },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.Center),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            ) {}
                            if (!recordingLogicViewModel.isRecording) {
                                Button(
                                    onClick = { buttonClicked() },
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .align(Alignment.Center),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                ) {}
                            } else {
                                Button(
                                    onClick = { buttonClicked() },
                                    shape = RoundedCornerShape(20),
                                    modifier = Modifier
                                        .size(30.dp)
                                        .align(Alignment.Center)
                                        .graphicsLayer(rotationZ = buttonRotationAnimation),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                ) {}
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.height(42.dp)
                    ) {
                        val textStyle = TextStyle(
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(3f, 3f),
                                blurRadius = 10f
                            ),
                        )
                        if (!recordingLogicViewModel.isRecording) {
                            Text(
                                style = textStyle,
                                text = stringResource(R.string.start_recording_button),
                            )
                        } else {
                            Text(
                                style = textStyle,
                                text = stringResource(R.string.stop_recording_button),
                            )
                        }
                    }
                }
            }

            if (recordingLogicViewModel.isRecording) {
                Button(
                    onClick = { saveRecording(recordingLogicViewModel) },
                    modifier = Modifier.align(Alignment.Center),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.hsl(
                            44f,
                            1f,
                            0.65f
                        )
                    ),
                    shape = RoundedCornerShape(12),
                ) {
                    Text(
                        text = stringResource(R.string.capture_button),
                        modifier = Modifier.padding(6.dp),
                        style = TextStyle(
                            letterSpacing = 0.1.em,
                            fontSize = 48.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold,
                        )
                    )
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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (recordingLogicViewModel.hasPermissions()) {
            CameraPreview(context, previewView, cameraController, lifecycleOwner) {
                onCameraInitialized()
            }
        } else {
            NoCameraPermissions(context) {
                // Callback to update permissions
                recordingLogicViewModel.setPermissions(hasPermissions(context))
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
        Text(text = stringResource(R.string.start_camera_button))
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


fun isFolderEmpty(context: Context): Boolean {
    val directory: File = context.filesDir
    val files = directory.listFiles()
    return files == null || files.isEmpty()
}

@SuppressLint("NewApi")
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

    if (uri != Uri.EMPTY) {
        val uriEncoded = URLEncoder.encode(
            uri.toString(),
            StandardCharsets.UTF_8.toString()
        )
    }

    val files = VideoHelper.getAllVideosInFolder(context.filesDir, "mp4")

    if (recordingLogicViewModel.saveRequested()) {
        VideoHelper.stitchAllVideosInFolder(context, context.filesDir, "mp4") { success ->
            if (success) {
                VideoHelper.deleteAllVideosInFolder(context.filesDir, "mp4")
                context.mainExecutor.execute {
                    Toast.makeText(context, "Video Captured!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("Camera", "Stitching videos failed! not deleting video sections")
            }
        }

        startRecording(context, previewView, videoCapture, lifecycleOwner, cameraController, recordingLogicViewModel)
        recordingLogicViewModel.clearSaveRequest()
    } else {
        val mediaFiles = files.sortedByDescending { it.lastModified() }
        for (i in 3 until mediaFiles.size) {
            Log.i("camera", "Deleting file: ${mediaFiles[i].name}")
            mediaFiles[i].delete()
        }

        if (uri != Uri.EMPTY) {
            val uriEncoded = URLEncoder.encode(
                uri.toString(),
                StandardCharsets.UTF_8.toString()
            )
        }
        if (recordingLogicViewModel.autoSaveRequested()) {
            startRecording(context, previewView, videoCapture, lifecycleOwner, cameraController, recordingLogicViewModel)
            recordingLogicViewModel.clearAutoSaveRequest()
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
    startRecordingTimer(recordingLogicViewModel)
}



private var handler: Handler? = null
private var runnable: Runnable? = null

fun startRecordingTimer(recordingLogicViewModel: RecordingLogicViewModel) {
    handler = Handler(Looper.getMainLooper())
    runnable = object : Runnable {
        override fun run() {
            recordingLogicViewModel.autoSave()
            handler?.postDelayed(this, recordingLogicViewModel.intervalMillis)
        }
    }
    handler?.postDelayed(runnable!!, recordingLogicViewModel.intervalMillis)
}

fun stopRecordingTimer() {
    handler?.removeCallbacks(runnable!!)
    handler = null
}


fun saveRecording(recordingLogicViewModel: RecordingLogicViewModel) {
    Log.d("Camera", "Save recording called")
    recordingLogicViewModel.requestSave()
    stopRecording(recordingLogicViewModel)
}

fun stopRecording(recordingLogicViewModel: RecordingLogicViewModel) {
    Log.d("Camera", "Stop recording called")
    recordingLogicViewModel.stopRecording()
    stopRecordingTimer()
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