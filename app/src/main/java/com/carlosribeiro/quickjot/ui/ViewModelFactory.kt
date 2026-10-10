package com.carlosribeiro.quickjot.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.carlosribeiro.quickjot.QuickJotApp
import com.carlosribeiro.quickjot.ui.editor.EditorViewModel
import com.carlosribeiro.quickjot.ui.folders.FoldersViewModel
import com.carlosribeiro.quickjot.ui.notes.NotesViewModel

val QuickJotViewModelFactory = viewModelFactory {
    initializer { FoldersViewModel(app().repository) }
    initializer { NotesViewModel(createSavedStateHandle(), app().repository) }
    initializer { EditorViewModel(createSavedStateHandle(), app().repository, app().applicationScope) }
}

private fun CreationExtras.app(): QuickJotApp = this[APPLICATION_KEY] as QuickJotApp
