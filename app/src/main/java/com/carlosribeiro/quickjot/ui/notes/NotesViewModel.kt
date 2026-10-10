package com.carlosribeiro.quickjot.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.carlosribeiro.quickjot.data.NameError
import com.carlosribeiro.quickjot.data.Note
import com.carlosribeiro.quickjot.data.QuickJotRepository
import com.carlosribeiro.quickjot.ui.NotesRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NoteDialog {
    data class Rename(val note: Note, val error: NameError? = null) : NoteDialog
    data class Delete(val note: Note) : NoteDialog
}

data class NotesUiState(
    val folderId: Long,
    val folderName: String = "",
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val dialog: NoteDialog? = null,
)

class NotesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: QuickJotRepository,
) : ViewModel() {

    private val folderId = savedStateHandle.toRoute<NotesRoute>().folderId
    private val dialog = MutableStateFlow<NoteDialog?>(null)

    val uiState: StateFlow<NotesUiState> = combine(
        repository.observeFolder(folderId),
        repository.observeNotes(folderId),
        dialog,
    ) { folder, notes, dialog ->
        NotesUiState(
            folderId = folderId,
            folderName = folder?.name.orEmpty(),
            isLoading = false,
            notes = notes,
            dialog = dialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState(folderId))

    fun onRenameClick(note: Note) {
        dialog.value = NoteDialog.Rename(note)
    }

    fun onDeleteClick(note: Note) {
        dialog.value = NoteDialog.Delete(note)
    }

    fun onDialogDismiss() {
        dialog.value = null
    }

    fun onRenameConfirm(title: String) {
        val current = dialog.value as? NoteDialog.Rename ?: return
        viewModelScope.launch {
            dialog.value = repository.renameNote(current.note.id, title)?.let { current.copy(error = it) }
        }
    }

    fun onDeleteConfirm() {
        val current = dialog.value as? NoteDialog.Delete ?: return
        dialog.value = null
        viewModelScope.launch { repository.deleteNote(current.note.id) }
    }
}
