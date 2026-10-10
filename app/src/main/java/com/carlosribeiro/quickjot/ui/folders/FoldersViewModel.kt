package com.carlosribeiro.quickjot.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosribeiro.quickjot.data.FolderWithCount
import com.carlosribeiro.quickjot.data.NameError
import com.carlosribeiro.quickjot.data.QuickJotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FolderDialog {
    data class Create(val error: NameError? = null) : FolderDialog
    data class Rename(val folder: FolderWithCount, val error: NameError? = null) : FolderDialog
    data class Delete(val folder: FolderWithCount) : FolderDialog
}

data class FoldersUiState(
    val isLoading: Boolean = true,
    val folders: List<FolderWithCount> = emptyList(),
    val dialog: FolderDialog? = null,
)

class FoldersViewModel(private val repository: QuickJotRepository) : ViewModel() {

    private val dialog = MutableStateFlow<FolderDialog?>(null)

    val uiState: StateFlow<FoldersUiState> =
        combine(repository.observeFolders(), dialog) { folders, dialog ->
            FoldersUiState(isLoading = false, folders = folders, dialog = dialog)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoldersUiState())

    fun onCreateClick() {
        dialog.value = FolderDialog.Create()
    }

    fun onRenameClick(folder: FolderWithCount) {
        dialog.value = FolderDialog.Rename(folder)
    }

    fun onDeleteClick(folder: FolderWithCount) {
        dialog.value = FolderDialog.Delete(folder)
    }

    fun onDialogDismiss() {
        dialog.value = null
    }

    fun onNameConfirm(name: String) {
        val current = dialog.value
        viewModelScope.launch {
            dialog.value = when (current) {
                is FolderDialog.Create ->
                    repository.createFolder(name)?.let { current.copy(error = it) }
                is FolderDialog.Rename ->
                    repository.renameFolder(current.folder.id, name)?.let { current.copy(error = it) }
                else -> current
            }
        }
    }

    fun onDeleteConfirm() {
        val current = dialog.value as? FolderDialog.Delete ?: return
        dialog.value = null
        viewModelScope.launch { repository.deleteFolder(current.folder.id) }
    }
}
