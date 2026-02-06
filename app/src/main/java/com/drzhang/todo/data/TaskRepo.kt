package com.drzhang.todo.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun getTasks(tab: TaskTab): Flow<List<TaskEntity>> =
        when (tab) {
            TaskTab.TODO -> dao.getTodoTasks()
            TaskTab.DONE -> dao.getDoneTasks()
            TaskTab.ALL -> dao.getAllTasks()
        }

    suspend fun addTask(task: TaskEntity) = dao.insert(task)

    suspend fun updateTask(task: TaskEntity) = dao.update(task)

    suspend fun deleteTask(task: TaskEntity) = dao.delete(task)
}
