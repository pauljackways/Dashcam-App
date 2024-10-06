package nz.ac.canterbury.seng303.lab2.models

data class AppSettings(
    val videoLength: Int = 30,
    val videoQuality: String = "High",
    val crashSensitivity: Float = 0.5f
)