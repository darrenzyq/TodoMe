package com.drzhang.todo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task ORDER BY taskDate DESC, priority ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE status = 0 ORDER BY taskDate DESC, priority ASC")
    fun getTodoTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE status = 1 ORDER BY taskDate DESC, priority ASC")
    fun getDoneTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}