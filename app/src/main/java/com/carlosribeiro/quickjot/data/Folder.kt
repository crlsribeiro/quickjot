package com.carlosribeiro.quickjot.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    indices = [Index(value = ["name"], unique = true)],
)
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

data class FolderWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val noteCount: Int,
)
