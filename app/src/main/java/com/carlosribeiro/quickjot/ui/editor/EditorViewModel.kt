package com.carlosribeiro.quickjot.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.carlosribeiro.quickjot.data.QuickJotRepository
import com.carlosribeiro.quickjot.ui.EditorRoute
import com.carlosribeiro.quickjot.ui.NEW_NOTE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EditorUiState(
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    /** True once the note exists in the database. */
    val isSaved: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isDeleted: Boolean = false,
)

class EditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: QuickJotRepository,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EditorRoute>()

    // The draft and the id of a note created here are mirrored into SavedStateHandle so they
    // survive process death, not only rotation.
    private var noteId: Long? =
        savedStateHandle[KEY_NOTE_ID] ?: route.noteId.takeIf { it != NEW_NOTE_ID }

    private val saveMutex = Mutex()

    private val _uiState = MutableStateFlow(
        EditorUiState(
            title = savedStateHandle[KEY_TITLE] ?: "",
            content = savedStateHandle[KEY_CONTENT] ?: "",
            isLoading = noteId != null && !savedStateHandle.contains(KEY_TITLE),
            isSaved = noteId != null,
        ),
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        val id = noteId
        if (id != null && _uiState.value.isLoading) {
            viewModelScope.launch {
                val note = repository.getNote(id)
                savedStateHandle[KEY_TITLE] = note?.title.orEmpty()
                savedStateHandle[KEY_CONTENT] = note?.content.orEmpty()
                _uiState.update {
                    it.copy(
                        title = note?.title.orEmpty(),
                        content = note?.content.orEmpty(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        savedStateHandle[KEY_TITLE] = title
        _uiState.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        savedStateHandle[KEY_CONTENT] = content
        _uiState.update { it.copy(content = content) }
    }

    /** Called when leaving the editor and when the app goes to the background. */
    fun save() {
        val state = _uiState.value
        if (state.isLoading || state.isDeleted) return
        applicationScope.launch {
            // Serialized so two saves of a new note can't both insert it.
            saveMutex.withLock {
                if (_uiState.value.isDeleted) return@withLock
                val id = repository.saveNote(noteId, route.folderId, state.title, state.content)
                if (id != null) {
                    noteId = id
                    savedStateHandle[KEY_NOTE_ID] = id
                    _uiState.update { it.copy(isSaved = true) }
                }
            }
        }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun onDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmation = false, isDeleted = true) }
        applicationScope.launch {
            saveMutex.withLock { noteId?.let { repository.deleteNote(it) } }
        }
    }

    private companion object {
        const val KEY_NOTE_ID = "savedNoteId"
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
    }
}
