package nz.ac.canterbury.seng303.lab2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nz.ac.canterbury.seng303.lab2.screens.GalleryScreen
import nz.ac.canterbury.seng303.lab2.screens.MainScreen
import nz.ac.canterbury.seng303.lab2.screens.Settings
import nz.ac.canterbury.seng303.lab2.ui.theme.Lab1Theme
import nz.ac.canterbury.seng303.lab2.util.Accelerometer
import nz.ac.canterbury.seng303.lab2.viewmodels.RecordingLogicViewModel
import nz.ac.canterbury.seng303.lab2.util.SpeedDetectionService
import nz.ac.canterbury.seng303.lab2.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.viewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity(){

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Lab1Theme {
                val navController = rememberNavController()
                Scaffold() {
                    Box(modifier = Modifier.padding(it)) {
                        NavHost(navController = navController, startDestination = "Home") {
                            composable("Home") {
                                MainScreen(navController = navController)
                            }

                            composable("Settings") {
                                Settings(navController = navController)
                            }

                            composable("Gallery") {
//                                val settingsViewModel = koinViewModel<SettingsViewModel>()
                                GalleryScreen(navController)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, SpeedDetectionService::class.java)
        stopService(intent)
    }

    override fun onStop() {
        super.onStop()
        SpeedDetectionService.start(this)
    }
}
