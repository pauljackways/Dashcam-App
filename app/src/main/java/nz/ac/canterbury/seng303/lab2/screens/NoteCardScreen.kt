package nz.ac.canterbury.seng303.lab2.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nz.ac.canterbury.seng303.lab2.models.Note
import nz.ac.canterbury.seng303.lab2.util.convertTimestampToReadableTime
import nz.ac.canterbury.seng303.lab2.viewmodels.NoteViewModel

@Composable
fun NoteCard(noteId: String, noteViewModel: NoteViewModel) {
    noteViewModel.getNoteById(noteId = noteId.toIntOrNull())
    val selectedNoteState by noteViewModel.selectedNote.collectAsState(null)
    val note: Note? = selectedNoteState // we explicitly assign to note to help the compilers smart cast out
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        if (note != null) {
            Text(text = "Title: ${note.title}", style = MaterialTheme.typography.headlineMedium)
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            Text(
                text = "Content:",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp, 0.dp, 0.dp, 0.dp)
            )
            Row {
                Text(
                    text = "Timestamp: ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = convertTimestampToReadableTime(note.timestamp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Archived: ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Checkbox(
                    checked = note.isArchived,
                    onCheckedChange = null, // No click event
                    enabled = false // Make the checkbox disabled
                )
            }
        } else {
            Text(text = "Could not find note: $noteId", style = MaterialTheme.typography.headlineMedium)
        }
    }
}