package com.carlosribeiro.quickjot.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlosribeiro.quickjot.R
import com.carlosribeiro.quickjot.data.FolderWithCount
import com.carlosribeiro.quickjot.data.NameError
import com.carlosribeiro.quickjot.ui.QuickJotViewModelFactory
import com.carlosribeiro.quickjot.ui.components.ConfirmDeleteDialog
import com.carlosribeiro.quickjot.ui.components.EmptyState
import com.carlosribeiro.quickjot.ui.components.NameDialog
import com.carlosribeiro.quickjot.ui.components.RowActionsMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    onOpenFolder: (Long) -> Unit,
    viewModel: FoldersViewModel = viewModel(factory = QuickJotViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::onCreateClick,
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text(stringResource(R.string.folders_new)) },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Unit
            state.folders.isEmpty() -> EmptyState(
                icon = R.drawable.ic_folder,
                message = stringResource(R.string.folders_empty),
                modifier = Modifier.padding(innerPadding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                // Keeps the last row clear of the FAB.
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(state.folders, key = { it.id }) { folder ->
                    FolderRow(
                        folder = folder,
                        onClick = { onOpenFolder(folder.id) },
                        onRename = { viewModel.onRenameClick(folder) },
                        onDelete = { viewModel.onDeleteClick(folder) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    when (val dialog = state.dialog) {
        is FolderDialog.Create -> NameDialog(
            title = stringResource(R.string.folders_new),
            label = stringResource(R.string.folder_name_label),
            confirmLabel = stringResource(R.string.action_create),
            initialValue = "",
            errorMessage = dialog.error?.message(),
            onConfirm = viewModel::onNameConfirm,
            onDismiss = viewModel::onDialogDismiss,
        )
        is FolderDialog.Rename -> NameDialog(
            title = stringResource(R.string.folder_rename_title),
            label = stringResource(R.string.folder_name_label),
            confirmLabel = stringResource(R.string.action_save),
            initialValue = dialog.folder.name,
            errorMessage = dialog.error?.message(),
            onConfirm = viewModel::onNameConfirm,
            onDismiss = viewModel::onDialogDismiss,
        )
        is FolderDialog.Delete -> ConfirmDeleteDialog(
            title = stringResource(R.string.folder_delete_title),
            message = with(dialog.folder) {
                if (noteCount == 0) {
                    stringResource(R.string.folder_delete_message_empty, name)
                } else {
                    pluralStringResource(R.plurals.folder_delete_message, noteCount, name, noteCount)
                }
            },
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDialogDismiss,
        )
        null -> Unit
    }
}

@Composable
private fun FolderRow(
    folder: FolderWithCount,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        headlineContent = { Text(folder.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(
                if (folder.noteCount == 0) {
                    stringResource(R.string.folder_no_notes)
                } else {
                    pluralStringResource(R.plurals.folder_note_count, folder.noteCount, folder.noteCount)
                },
            )
        },
        trailingContent = { RowActionsMenu(onRename = onRename, onDelete = onDelete) },
    )
}

@Composable
private fun NameError.message(): String = when (this) {
    NameError.EMPTY -> stringResource(R.string.error_name_empty)
    NameError.DUPLICATE -> stringResource(R.string.error_name_duplicate)
}
