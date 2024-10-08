package nz.ac.canterbury.seng303.lab2.util

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

object VideoHelper {
    /*
    Uses the last modified date of the files to sort them oldest to newest
    File extensions should not include the full stop, e.g., "mp4" rather than ".mp4"
     */
    fun stitchAllVideosInFolder(context: Context, folderPath: String,
                                videoFileExtension: String, outputPath: String) {
        val folder = File(folderPath)
        if (!folder.exists())
            throw Exception("Folder path not found!")
        if (!folder.isDirectory)
            throw Exception("Folder path is not a directory!")

        val filePaths = folder.listFiles()
            ?.filter { it.isFile && it.extension == videoFileExtension }
            ?.sortedBy { it.lastModified() }
            ?.map { it.absolutePath } ?: emptyList()
        if (filePaths.isEmpty()) {
            println("Folder doesn't contain any videos with file extension <filename>.$videoFileExtension")
            return
        }

        stitchVideosTogether(context, filePaths, outputPath)
    }

    fun stitchVideosTogether(context: Context, inputPaths: List<String>, outputPath: String) {
        // this seems a bit cursed but it's the best way I could find to safely concatenate
        // video files without the meta-data causing issues.
        val tempFile = File(context.cacheDir, "ffmpeg_input_list.txt")
        tempFile.bufferedWriter().use { writer ->
            inputPaths.forEach { path ->
                writer.write("file '$path'\n")
            }
        }

        val ffmpegCommand = "-f concat -safe 0 -i ${tempFile.absolutePath} -c copy $outputPath"

        FFmpegKit.executeAsync(ffmpegCommand) { session ->
            if (!session.returnCode.isValueSuccess) {
                println("Stitching videos failed!: ${session.returnCode.value}")
            }

            tempFile.delete()
        }
    }
}