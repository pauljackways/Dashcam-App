package nz.ac.canterbury.seng303.lab2.screens

import android.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.lab2.models.Note
import nz.ac.canterbury.seng303.lab2.util.convertTimestampToReadableTime
import nz.ac.canterbury.seng303.lab2.viewmodels.EditNoteViewModel
import nz.ac.canterbury.seng303.lab2.viewmodels.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNote(
    noteId: String,
    editNoteViewModel: EditNoteViewModel,
    noteViewModel: NoteViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val selectedNoteState by noteViewModel.selectedNote.collectAsState(null)
    val note: Note? = selectedNoteState // we explicitly assign to note to help the compilers smart cast out

    LaunchedEffect(note) {  // Get the default values for the note properties
        if (note == null) {
            noteViewModel.getNoteById(noteId.toIntOrNull())
        } else {
            editNoteViewModel.setDefaultValues(note)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = editNoteViewModel.title,
            onValueChange = { editNoteViewModel.updateTitle(it) },
            label = { Text("Title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = editNoteViewModel.content,
            onValueChange = { editNoteViewModel.updateContent(it) },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .fillMaxHeight()
                .weight(1f)
        )
        OutlinedTextField(
            value = convertTimestampToReadableTime(editNoteViewModel.timestamp),
            onValueChange = { },
            label = { Text("Timestamp") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
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
                checked = editNoteViewModel.isArchived,
                onCheckedChange = {editNoteViewModel.updateIsArchived(it)}
            )
        }
        Button(
            onClick = {
                noteViewModel.editNoteById(noteId.toIntOrNull(), note = Note(noteId.toInt(), editNoteViewModel.title, editNoteViewModel.content, editNoteViewModel.timestamp, editNoteViewModel.isArchived))
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Edited note!")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, id ->
                        navController.navigate("noteList")
                    }
                val alert = builder.create()
                alert.show()

            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "Save")
        }
    }
}
