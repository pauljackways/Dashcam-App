package nz.ac.canterbury.seng303.lab2.util

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.provider.MediaStore
import android.widget.LinearLayout
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.CameraSelector
import androidx.camera.video.VideoCapture
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import java.io.File

object Camera {
    var isRecording by mutableStateOf(false)

    private const val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private fun hasPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    @Composable
    fun CameraPreview(context: Context) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraController = remember { LifecycleCameraController(context) }

        // Ensure that the camera is being bound to the lifecycle
        DisposableEffect(Unit) {
            cameraController.bindToLifecycle(lifecycleOwner)
            onDispose {
                cameraController.unbind() // Clean up resources when composable is disposed
            }
        }

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
    @Composable
    fun InitCamera() {
        val context = LocalContext.current
        var hasPermissions by remember { mutableStateOf(false)}

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasPermissions) {
                CameraPreview(context)
            } else {
                NoCameraPermissions(context) {
                    // Callback to update permissions
                    hasPermissions = hasPermissions(context)
                }
            }
        }
    }
    fun startRecording(context: Context) {

    }

    fun saveRecording() {

    }
    fun stopRecording() {

    }
}