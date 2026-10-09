package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity): Long

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_entries WHERE toolType = :toolType ORDER BY timestamp DESC")
    fun getHistoryByTool(toolType: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_entries WHERE inputText LIKE '%' || :query || '%' OR outputText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<HistoryEntity>>

    @Query("DELETE FROM history_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history_entries")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM history_entries")
    suspend fun getCount(): Int
}
