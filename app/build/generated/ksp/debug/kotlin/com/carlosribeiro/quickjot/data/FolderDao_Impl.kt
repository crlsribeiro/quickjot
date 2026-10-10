package com.carlosribeiro.quickjot.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FolderDao_Impl(
  __db: RoomDatabase,
) : FolderDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFolder: EntityInsertAdapter<Folder>
  init {
    this.__db = __db
    this.__insertAdapterOfFolder = object : EntityInsertAdapter<Folder>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `folders` (`id`,`name`,`createdAt`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Folder) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.createdAt)
      }
    }
  }

  public override suspend fun insert(folder: Folder): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfFolder.insertAndReturnId(_connection, folder)
    _result
  }

  public override fun observeAllWithCount(): Flow<List<FolderWithCount>> {
    val _sql: String = """
        |
        |        SELECT f.id, f.name, f.createdAt, COUNT(n.id) AS noteCount
        |        FROM folders f
        |        LEFT JOIN notes n ON n.folderId = f.id
        |        GROUP BY f.id
        |        ORDER BY f.name COLLATE NOCASE
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("folders", "notes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfCreatedAt: Int = 2
        val _columnIndexOfNoteCount: Int = 3
        val _result: MutableList<FolderWithCount> = mutableListOf()
        while (_stmt.step()) {
          val _item: FolderWithCount
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpNoteCount: Int
          _tmpNoteCount = _stmt.getLong(_columnIndexOfNoteCount).toInt()
          _item = FolderWithCount(_tmpId,_tmpName,_tmpCreatedAt,_tmpNoteCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeById(id: Long): Flow<Folder?> {
    val _sql: String = "SELECT * FROM folders WHERE id = ?"
    return createFlow(__db, false, arrayOf("folders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: Folder?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = Folder(_tmpId,_tmpName,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getNames(excludeId: Long): List<String> {
    val _sql: String = "SELECT name FROM folders WHERE id != ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, excludeId)
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun rename(id: Long, name: String) {
    val _sql: String = "UPDATE folders SET name = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM folders WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
