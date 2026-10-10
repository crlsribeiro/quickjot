package com.carlosribeiro.quickjot.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Folder::class, Note::class], version = 1, exportSchema = false)
abstract class QuickJotDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao

    companion object {
        fun create(context: Context): QuickJotDatabase =
            Room.databaseBuilder(context, QuickJotDatabase::class.java, "quickjot.db").build()
    }
}
