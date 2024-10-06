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

fun convertTimestampToVideoTitle(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd_MMMM_yyyy_HH_mm", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val seconds = calendar.get(Calendar.SECOND)
    val milliseconds = calendar.get(Calendar.MILLISECOND)
    val formattedDate = dateFormat.format(calendar.time)
    return "${formattedDate}_${seconds}${milliseconds.toString().padStart(3, '0')}.mp4"
}