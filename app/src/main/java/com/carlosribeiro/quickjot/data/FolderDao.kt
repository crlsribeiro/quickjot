package com.carlosribeiro.quickjot.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query(
        """
        SELECT f.id, f.name, f.createdAt, COUNT(n.id) AS noteCount
        FROM folders f
        LEFT JOIN notes n ON n.folderId = f.id
        GROUP BY f.id
        ORDER BY f.name COLLATE NOCASE
        """,
    )
    fun observeAllWithCount(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders WHERE id = :id")
    fun observeById(id: Long): Flow<Folder?>

    @Query("SELECT name FROM folders WHERE id != :excludeId")
    suspend fun getNames(excludeId: Long): List<String>

    @Insert
    suspend fun insert(folder: Folder): Long

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
