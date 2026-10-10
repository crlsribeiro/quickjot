package com.carlosribeiro.quickjot.ui.notes

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlosribeiro.quickjot.R
import com.carlosribeiro.quickjot.data.Note
import com.carlosribeiro.quickjot.ui.QuickJotViewModelFactory
import com.carlosribeiro.quickjot.ui.components.ConfirmDeleteDialog
import com.carlosribeiro.quickjot.ui.components.EmptyState
import com.carlosribeiro.quickjot.ui.components.NameDialog
import com.carlosribeiro.quickjot.ui.components.RowActionsMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onOpenEditor: (folderId: Long, noteId: Long?) -> Unit,
    viewModel: NotesViewModel = viewModel(factory = QuickJotViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.folderName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onOpenEditor(state.folderId, null) },
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text(stringResource(R.string.notes_new)) },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Unit
            state.notes.isEmpty() -> EmptyState(
                icon = R.drawable.ic_note,
                message = stringResource(R.string.notes_empty),
                modifier = Modifier.padding(innerPadding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                // Keeps the last row clear of the FAB.
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(state.notes, key = { it.id }) { note ->
                    Column(modifier = Modifier.animateItem()) {
                        NoteRow(
                            note = note,
                            onClick = { onOpenEditor(state.folderId, note.id) },
                            onRename = { viewModel.onRenameClick(note) },
                            onDelete = { viewModel.onDeleteClick(note) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    when (val dialog = state.dialog) {
        is NoteDialog.Rename -> NameDialog(
            title = stringResource(R.string.note_rename_title),
            label = stringResource(R.string.note_title_label),
            confirmLabel = stringResource(R.string.action_save),
            initialValue = dialog.note.title,
            errorMessage = dialog.error?.let { stringResource(R.string.error_note_empty) },
            onConfirm = viewModel::onRenameConfirm,
            onDismiss = viewModel::onDialogDismiss,
        )
        is NoteDialog.Delete -> ConfirmDeleteDialog(
            title = stringResource(R.string.note_delete_title),
            message = stringResource(R.string.note_delete_message, dialog.note.displayTitle()),
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDialogDismiss,
        )
        null -> Unit
    }
}

@Composable
private fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(note.displayTitle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column {
                // When there is no title the first line of the text is already the headline.
                if (note.title.isNotBlank() && note.content.isNotBlank()) {
                    Text(note.content.trim(), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    text = DateUtils.formatDateTime(
                        context,
                        note.updatedAt,
                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        },
        trailingContent = { RowActionsMenu(onRename = onRename, onDelete = onDelete) },
    )
}

@Composable
private fun Note.displayTitle(): String =
    title.ifBlank { content.lineSequence().firstOrNull { it.isNotBlank() }?.trim() }
        ?: stringResource(R.string.note_untitled)
