package nz.ac.canterbury.seng303.lab2.util


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import android.widget.LinearLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.video.AudioConfig
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import nz.ac.canterbury.seng303.lab2.viewmodels.CameraStateViewModel
import java.io.File


class Camera(private val cameraStateViewModel: CameraStateViewModel) {

    private var recording: Recording? = null

    private val requestCodePermissions = 1001
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

    @Composable
    fun InitCamera() {
        val context = LocalContext.current
        var hasPermissions by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasPermissions) {
                CameraPreview(context, cameraStateViewModel)
            } else {
                NoCameraPermissions(context) {
                    // Callback to update permissions
                    hasPermissions = hasPermissions(context)
                }
            }
        }
    }

    @SuppressLint("MissingPermission") // handled elsewhere
    @Composable
    fun CameraPreview(context: Context, cameraStateViewModel: CameraStateViewModel) {
        if (!hasPermissions(context)) {
            recording?.stop()
            recording = null
            return
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraController = remember { LifecycleCameraController(context) }

        DisposableEffect(Unit) {
            cameraController.bindToLifecycle(lifecycleOwner)
            onDispose {
                cameraController.unbind()
            }
        }

        AndroidView(factory = { innerContext ->
            PreviewView(innerContext).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_START
                controller = cameraController
            }
        })

        if (cameraStateViewModel.isRecording() && recording == null) {

            val outputFile = File(context.filesDir, convertTimestampToVideoTitle(System.currentTimeMillis()))

            recording = cameraController.startRecording(
                FileOutputOptions.Builder(outputFile).build(),
                AudioConfig.create(true),
                ContextCompat.getMainExecutor(context)
            ) { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> handleFinalizeEvent(event, outputFile)
                }
            }

        } else if (!cameraStateViewModel.isRecording() && recording != null) {
            recording?.stop() // calls handleFinalizeEvent
            recording = null // Safety
        }
    }


    private fun handleFinalizeEvent(finalizeEvent: VideoRecordEvent.Finalize, outputFile: File) {
        recording = null
        if (finalizeEvent.hasError()) {
            outputFile.delete() // Clean up file on error
        } else if (cameraStateViewModel.saveRequested()) {
            // Save
            cameraStateViewModel.clearSaveRequest()
        }
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

    fun startRecording() {
        Log.d("Camera", "Start recording called")
        cameraStateViewModel.startRecording()
    }

    fun saveRecording() {
        Log.d("Camera", "Save recording called")
        cameraStateViewModel.saveRecording()
    }

    fun stopRecording() {
        Log.d("Camera", "Stop recording called")
        cameraStateViewModel.stopRecording()
    }
}
