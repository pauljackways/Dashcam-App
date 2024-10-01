package nz.ac.canterbury.seng303.lab2.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.lab2.R
import nz.ac.canterbury.seng303.lab2.models.Note
import nz.ac.canterbury.seng303.lab2.util.convertTimestampToReadableTime
import nz.ac.canterbury.seng303.lab2.viewmodels.NoteViewModel

@Composable
fun NoteGrid(navController: NavController, noteViewModel: NoteViewModel) {
    noteViewModel.getNotes()
    val notes: List<Note> by noteViewModel.notes.collectAsState(emptyList())
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Specify the number of columns in the grid
        contentPadding = PaddingValues(4.dp, 8.dp),
        modifier = Modifier.background(Color.LightGray)
    ) {
        items(notes) { note ->
            NoteGridItem(navController = navController, note = note)
        }
    }
}

@Composable
fun NoteGridItem(navController: NavController, note: Note) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(2.dp)
                .fillMaxWidth()
        ) {
            Box {
                Image(
                    painter = painterResource(id = if (note.isArchived) R.drawable.note_bw else R.drawable.note),
                    contentDescription = "Note Image",
                    modifier = Modifier
                        .height(120.dp) // Adjust the image height as needed
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                )
                TextButton(
                    onClick = { navController.navigate("NoteCard/${note.id}") },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(text = "View")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = convertTimestampToReadableTime(note.timestamp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
