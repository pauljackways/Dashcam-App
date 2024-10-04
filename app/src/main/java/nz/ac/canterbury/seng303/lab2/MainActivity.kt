package nz.ac.canterbury.seng303.lab2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nz.ac.canterbury.seng303.lab2.screens.MainScreen
import nz.ac.canterbury.seng303.lab2.screens.Settings
import nz.ac.canterbury.seng303.lab2.ui.theme.Lab1Theme
import nz.ac.canterbury.seng303.lab2.util.Accelerometer


class MainActivity : ComponentActivity(), Accelerometer.AccelerometerListener {

    private lateinit var accelerometer: Accelerometer

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accelerometer = Accelerometer(this, this)

        setContent {
            Lab1Theme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("303 a2") },
                            navigationIcon = {
                                IconButton(onClick = { navController.navigate("Home") }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) {

                    Box(modifier = Modifier.padding(it)) {
                        NavHost(navController = navController, startDestination = "Home") {
                            composable("Home") {
                                MainScreen(navController = navController)
                            }

                            composable("Settings") {
                                Settings(navController)
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onAccelerationChanged(x: Float, y: Float, z: Float) {
        System.out.println("Accelerometer: x: $x, y: $y, z: $z")
}

    override fun onCrashDetected() {
        println("Crash detected!")
    }

    override fun onResume() {
        super.onResume()
        accelerometer.start()
    }

    override fun onPause() {
        super.onPause()
        accelerometer.stop()
    }
}

