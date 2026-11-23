package com.example.studenttaskmanager.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // --- Dashboard queries ---
    @Query("SELECT COUNT(*) FROM tasks")
    fun observeTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status")
    fun observeCount(status: TaskStatus): Flow<Int>

    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<Task>>

    // --- Tasks screen queries ---
    @Query("SELECT * FROM tasks ORDER BY status ASC, deadline ASC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :q || '%' ORDER BY deadline ASC")
    fun search(q: String): Flow<List<Task>>

    // --- Writes ---
    @Insert suspend fun insert(task: Task): Long
    @Update suspend fun update(task: Task)
    @Delete suspend fun delete(task: Task)
    @Query("DELETE FROM tasks") suspend fun deleteAll()
}