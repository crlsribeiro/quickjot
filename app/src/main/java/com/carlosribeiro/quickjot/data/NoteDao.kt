package com.carlosribeiro.quickjot.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY updatedAt DESC, id DESC")
    fun observeByFolder(folderId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    @Insert
    suspend fun insert(note: Note): Long

    @Query("UPDATE notes SET title = :title, content = :content, updatedAt = :updatedAt WHERE id = :id")
    suspend fun update(id: Long, title: String, content: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
