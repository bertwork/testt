package com.example.studenttaskmanager.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenttaskmanager.data.AppDatabase
import com.example.studenttaskmanager.data.Task
import com.example.studenttaskmanager.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).taskDao()

    // --- Dashboard ---
    val total: StateFlow<Int> = dao.observeTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val inProgress: StateFlow<Int> = dao.observeCount(TaskStatus.IN_PROGRESS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val completed: StateFlow<Int> = dao.observeCount(TaskStatus.COMPLETED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val recent: StateFlow<List<Task>> = dao.observeRecent(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- Tasks screen ---
    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow(TaskFilter.ALL)

    val tasks: StateFlow<List<Task>> =
        combine(dao.observeAll(), _query, _filter) { all, q, f ->
            all.filter { t ->
                (q.isBlank() || t.title.contains(q, ignoreCase = true)) &&
                        when (f) {
                            TaskFilter.ALL       -> true
                            TaskFilter.ACTIVE    -> t.status == TaskStatus.IN_PROGRESS
                            TaskFilter.COMPLETED -> t.status == TaskStatus.COMPLETED
                        }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun setFilter(f: TaskFilter) { _filter.value = f }

    // --- Writes (wired in Checkpoint 2) ---
    fun addTask(title: String, deadline: Long, done: Boolean) = viewModelScope.launch {
        dao.insert(
            Task(
                title = title.trim(),
                deadline = deadline,
                status = if (done) TaskStatus.COMPLETED else TaskStatus.IN_PROGRESS
            )
        )
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        dao.update(task.copy(updatedAt = System.currentTimeMillis()))
    }

    fun toggleStatus(task: Task) = viewModelScope.launch {
        val next = if (task.status == TaskStatus.COMPLETED)
            TaskStatus.IN_PROGRESS else TaskStatus.COMPLETED
        dao.update(task.copy(status = next, updatedAt = System.currentTimeMillis()))
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        dao.delete(task)
    }
}