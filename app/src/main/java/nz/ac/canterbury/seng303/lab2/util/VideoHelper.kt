package nz.ac.canterbury.seng303.lab2.util

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

object VideoHelper {
    /*
    Uses the last modified date of the files to sort them oldest to newest
    File extensions should not include the full stop, e.g., "mp4" rather than ".mp4"
     */
    fun stitchAllVideosInFolder(context: Context, folder: File, videoFileExtension: String,
                                outputDirectory: File, onComplete: (Boolean) -> Unit) {
        val filePaths = getAllVideosInFolder(folder, videoFileExtension)
        if (filePaths.isEmpty()) {
            println("Folder doesn't contain any videos with file extension <filename>.$videoFileExtension")
            return
        }

        stitchVideosTogether(context, filePaths, outputDirectory, onComplete)
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
                             outputDirectory: File, onComplete: (Boolean) -> Unit) {
        // this seems a bit cursed but it's the best way I could find to safely concatenate
        // video files without the meta-data causing issues.
        val tempFile = File(context.cacheDir, "ffmpeg_input_list.txt")
        tempFile.bufferedWriter().use { writer ->
            inputPaths.forEach { path ->
                writer.write("file '${path.absolutePath}'\n")
            }
        }

        val ffmpegCommand = "-f concat -safe 0 -i ${tempFile.absolutePath} -c copy ${outputDirectory.absolutePath}"

        FFmpegKit.executeAsync(ffmpegCommand) { session ->
            if (!session.returnCode.isValueSuccess) {
                println("Stitching videos failed!: ${session.returnCode.value}")
            }

            onComplete(session.returnCode.isValueSuccess)

            tempFile.delete()
        }
    }
}