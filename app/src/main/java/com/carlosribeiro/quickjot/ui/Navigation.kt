package com.carlosribeiro.quickjot.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carlosribeiro.quickjot.ui.editor.EditorScreen
import com.carlosribeiro.quickjot.ui.folders.FoldersScreen
import com.carlosribeiro.quickjot.ui.notes.NotesScreen
import kotlinx.serialization.Serializable

@Serializable
data object FoldersRoute

@Serializable
data class NotesRoute(val folderId: Long)

/** [noteId] is [NEW_NOTE_ID] when creating a note. */
@Serializable
data class EditorRoute(val folderId: Long, val noteId: Long = NEW_NOTE_ID)

const val NEW_NOTE_ID = 0L

@Composable
fun QuickJotNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = FoldersRoute) {
        composable<FoldersRoute> {
            FoldersScreen(
                onOpenFolder = { folderId -> navController.navigate(NotesRoute(folderId)) },
            )
        }
        composable<NotesRoute> {
            NotesScreen(
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenEditor = { folderId, noteId ->
                    navController.navigate(EditorRoute(folderId, noteId ?: NEW_NOTE_ID))
                },
            )
        }
        composable<EditorRoute> {
            EditorScreen(onClose = dropUnlessResumed { navController.popBackStack() })
        }
    }
}
