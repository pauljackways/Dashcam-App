package nz.ac.canterbury.seng303.lab2.util

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

object VideoHelper {
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