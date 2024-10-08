package nz.ac.canterbury.seng303.lab2.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

object VideoHelper {
    private const val SAVED_VIDEO_ALBUM_NAME = "CrashGuard Recordings"

    /*
    Uses the last modified date of the files to sort them oldest to newest
    File extensions should not include the full stop, e.g., "mp4" rather than ".mp4"
     */
    fun stitchAllVideosInFolder(context: Context, folder: File, videoFileExtension: String,
                                onComplete: (Boolean) -> Unit) {
        val filePaths = getAllVideosInFolder(folder, videoFileExtension)
        if (filePaths.isEmpty()) {
            println("Folder doesn't contain any videos with file extension <filename>.$videoFileExtension")
            return
        }

        stitchVideosTogether(context, filePaths, onComplete)
    }

    fun deleteAllVideosInFolder(folder: File, videoFileExtension: String) {
        getAllVideosInFolder(folder, videoFileExtension).forEach { it.delete() }
    }

    /*
    Returns all the files in the folder in ascending order (oldest to newest) by last modified date
     */
    fun getAllVideosInFolder(folder: File,
                             videoFileExtension: String): List<File> {
        if (!folder.exists())
            throw Exception("Folder path not found!")
        if (!folder.isDirectory)
            throw Exception("Folder path is not a directory!")

        val files = folder.listFiles()
            ?.filter { it.isFile && it.extension == videoFileExtension }
            ?.sortedBy { it.lastModified() } ?: emptyList()
        return files
    }

    fun stitchVideosTogether(context: Context, inputPaths: List<File>,
                             onComplete: (Boolean) -> Unit) {
        // this seems a bit cursed but it's the best way I could find to safely concatenate
        // video files without the meta-data causing issues.
        val tempFile = File(context.cacheDir, "ffmpeg_input_list.txt")
        tempFile.bufferedWriter().use { writer ->
            inputPaths.forEach { path ->
                writer.write("file '${path.absolutePath}'\n")
            }
        }

        val filename = convertTimestampToVideoTitle(System.currentTimeMillis())
        val outputDirectory = File(getSavedVideoAlbum(), filename)
        Log.e("Video Helper", "e\ne\ne\ne\ne\ne\n $outputDirectory")
        val ffmpegCommand = "-f concat -safe 0 -i ${tempFile.absolutePath} -c copy \"${outputDirectory.absolutePath}\""

        FFmpegKit.executeAsync(ffmpegCommand) { session ->
            if (!session.returnCode.isValueSuccess) {
                println("Stitching videos failed!: ${session.returnCode.value}")
            }

            if (outputDirectory.exists()) {
                context.sendBroadcast(
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                        data = Uri.fromFile(outputDirectory)
                    }
                )
            }

            onComplete(session.returnCode.isValueSuccess)

            tempFile.delete()
        }
    }

    fun getSavedVideoAlbum(): File {
        val albumDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            SAVED_VIDEO_ALBUM_NAME
        )
        if (!albumDir.exists()) {
            albumDir.mkdirs()
            Log.i("Video Helper", "Created saved videos album")
        }

        return albumDir
    }
}