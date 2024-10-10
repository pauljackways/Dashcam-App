package nz.ac.canterbury.seng303.lab2.screens

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.ac.canterbury.seng303.lab2.util.VideoHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(navController: NavController) {
    val context = LocalContext.current
    val videos = VideoHelper.getAllVideosInFolder(VideoHelper.getSavedVideoAlbum(), "mp4")

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var thumbnails by remember { mutableStateOf<List<Bitmap?>>(List(videos.size) { null }) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(videos) {
        coroutineScope.launch {
            val generatedThumbnails = videos.map { videoFile ->
                withContext(Dispatchers.IO) {
                    ThumbnailUtils.createVideoThumbnail(videoFile.path, MediaStore.Video.Thumbnails.MINI_KIND)
                }
            }
            thumbnails = generatedThumbnails
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLandscape) Spacer(modifier = Modifier.weight(0.85f))
                        else Spacer(modifier = Modifier.weight(0.75f))
                        // TODO update to strings.xml
                        Text(text = "Gallery")
                        Spacer(modifier = Modifier.weight(1f))
                    }
                },
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
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        val modifier = if (isLandscape) {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        } else {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        }

        if (videos.isEmpty()) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TODO update with strings.xml
                Text(
                    fontSize = 26.sp,
                    text = "No videos saved yet",
                )
            }
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(

            ) {
                for ((index, videoFile) in videos.withIndex()) {
                    val name = videoFile.name.removeSuffix(".mp4").replace("_", " ")

                    item {
                        Button(
                            onClick = {
                                val albumUri: Uri = FileProvider.getUriForFile(context,
                                    "${context.packageName}.provider", videoFile)

                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(albumUri, "video/mp4")
                                    flags =
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                }

                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(top=12.dp, bottom=12.dp),
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                val thumbnail = thumbnails[index]
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    if (thumbnail != null) {
                                        Image(
                                            bitmap = thumbnail.asImageBitmap(),
                                            contentDescription = "Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize()
                                                .background(Color.Gray)
                                        ) {}
                                    }
                                }

                                Text(
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .align(Alignment.CenterVertically)
                                        .fillMaxWidth(),
                                    fontSize = 18.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    text = name,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}