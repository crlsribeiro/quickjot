package com.carlosribeiro.quickjot.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlosribeiro.quickjot.R
import com.carlosribeiro.quickjot.ui.QuickJotViewModelFactory
import com.carlosribeiro.quickjot.ui.components.ConfirmDeleteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onClose: () -> Unit,
    viewModel: EditorViewModel = viewModel(factory = QuickJotViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saveAndClose = {
        viewModel.save()
        onClose()
    }

    BackHandler(onBack = saveAndClose)
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.save() }
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isSaved) R.string.editor_title_edit else R.string.editor_title_new,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = saveAndClose) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (state.isSaved) {
                        IconButton(onClick = viewModel::onDeleteClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = stringResource(R.string.action_delete),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) return@Scaffold

        val titleFocus = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            if (!state.isSaved) titleFocus.requestFocus()
        }
        val fieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            TextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = {
                    Text(stringResource(R.string.note_title_label), style = MaterialTheme.typography.titleLarge)
                },
                textStyle = MaterialTheme.typography.titleLarge,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
            )
            TextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                placeholder = { Text(stringResource(R.string.note_content_hint)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }

    if (state.showDeleteConfirmation) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.note_delete_title),
            message = stringResource(
                R.string.note_delete_message,
                state.title.ifBlank { stringResource(R.string.note_untitled) },
            ),
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDeleteDismiss,
        )
    }
}
