package nz.ac.canterbury.seng303.lab2.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun convertTimestampToReadableTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return dateFormat.format(calendar.time)
}