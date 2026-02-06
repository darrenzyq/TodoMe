package com.drzhang.todo.mode

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.drzhang.todo.data.TaskEntity
import com.drzhang.todo.data.TaskPriority
import com.drzhang.todo.data.TaskRepository
import com.drzhang.todo.data.TaskStatus
import com.drzhang.todo.data.TaskTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    data class TabTasksUiState(
        val tasks: List<TaskEntity> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _currentTab = MutableStateFlow(TaskTab.TODO)
    val currentTab = _currentTab.asStateFlow()

    private val _tabTasksUiState: StateFlow<TabTasksUiState> =
        _currentTab.flatMapLatest { tab ->
            repository.getTasks(tab)
                .map { tasks ->
                    delay(500)
                    TabTasksUiState(tasks = tasks, isLoading = false)
                }
                .flowOn(Dispatchers.IO)
                .onStart { emit(TabTasksUiState(isLoading = true)) }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TabTasksUiState(isLoading = true)
        )
    val tasksState: StateFlow<TabTasksUiState> = _tabTasksUiState

    fun switchTab(tab: TaskTab) {
        _currentTab.value = tab
    }

    var editingTask by mutableStateOf<TaskEntity?>(null)
        private set

    var showEditor by mutableStateOf(false)
        private set

    fun addTask() {
        editingTask = null
        showEditor = true
    }

    fun editTask(task: TaskEntity) {
        if (task.status == TaskStatus.DONE.code) return
        editingTask = task
        showEditor = true
    }

    fun closeEditor() {
        showEditor = false
    }

    fun saveTask(
        content: String,
        priority: Int,
        date: String
    ) = viewModelScope.launch {

        val task = editingTask
        if (task == null) {
            repository.addTask(
                TaskEntity(
                    taskDate = date,
                    content = content,
                    status = TaskStatus.TODO.code,
                    priority = priority
                )
            )
        } else {
            repository.updateTask(
                task.copy(
                    taskDate = date,
                    content = content,
                    status = TaskStatus.TODO.code,
                    priority = priority
                )
            )
        }
        closeEditor()
    }

    fun markDone(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = 1))
        }
    }

    fun restore(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = 0))
        }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

}
