package com.carlosribeiro.quickjot.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

enum class NameError { EMPTY, DUPLICATE }

class QuickJotRepository(
    private val database: QuickJotDatabase,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val folderDao = database.folderDao()
    private val noteDao = database.noteDao()

    fun observeFolders(): Flow<List<FolderWithCount>> = folderDao.observeAllWithCount()

    fun observeFolder(id: Long): Flow<Folder?> = folderDao.observeById(id)

    fun observeNotes(folderId: Long): Flow<List<Note>> = noteDao.observeByFolder(folderId)

    suspend fun getNote(id: Long): Note? = noteDao.getById(id)

    /** Returns null on success, or the reason the name was rejected. */
    suspend fun createFolder(name: String): NameError? = database.withTransaction {
        val trimmed = name.trim()
        val error = validateFolderName(trimmed, excludeId = 0)
        if (error == null) folderDao.insert(Folder(name = trimmed, createdAt = now()))
        error
    }

    /** Returns null on success, or the reason the name was rejected. */
    suspend fun renameFolder(id: Long, name: String): NameError? = database.withTransaction {
        val trimmed = name.trim()
        val error = validateFolderName(trimmed, excludeId = id)
        if (error == null) folderDao.rename(id, trimmed)
        error
    }

    suspend fun deleteFolder(id: Long) = folderDao.deleteById(id)

    /**
     * Inserts the note when [id] is null, otherwise updates it. A note with neither title nor
     * text is not saved. Returns the id of the stored note, or null if nothing is stored.
     */
    suspend fun saveNote(id: Long?, folderId: Long, title: String, content: String): Long? =
        database.withTransaction {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isEmpty() && content.isBlank()) return@withTransaction id
            if (id == null) {
                val timestamp = now()
                return@withTransaction noteDao.insert(
                    Note(
                        folderId = folderId,
                        title = trimmedTitle,
                        content = content,
                        createdAt = timestamp,
                        updatedAt = timestamp,
                    ),
                )
            }
            val current = noteDao.getById(id) ?: return@withTransaction null
            if (current.title != trimmedTitle || current.content != content) {
                noteDao.update(id, trimmedTitle, content, now())
            }
            id
        }

    /** Returns null on success, or [NameError.EMPTY] if the note would end up with no title and no text. */
    suspend fun renameNote(id: Long, title: String): NameError? = database.withTransaction {
        val current = noteDao.getById(id) ?: return@withTransaction null
        val trimmed = title.trim()
        if (trimmed.isEmpty() && current.content.isBlank()) return@withTransaction NameError.EMPTY
        if (current.title != trimmed) noteDao.update(id, trimmed, current.content, now())
        null
    }

    suspend fun deleteNote(id: Long) = noteDao.deleteById(id)

    private suspend fun validateFolderName(trimmed: String, excludeId: Long): NameError? = when {
        trimmed.isEmpty() -> NameError.EMPTY
        // Compared in Kotlin because SQLite's NOCASE only folds ASCII ("Á" vs "á").
        folderDao.getNames(excludeId).any { it.equals(trimmed, ignoreCase = true) } -> NameError.DUPLICATE
        else -> null
    }
}
