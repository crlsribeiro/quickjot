package com.carlosribeiro.quickjot.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("folderId")],
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)
