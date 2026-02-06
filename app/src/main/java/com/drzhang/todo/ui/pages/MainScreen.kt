package com.drzhang.todo.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drzhang.todo.R
import com.drzhang.todo.data.TaskEntity
import com.drzhang.todo.data.TaskPriority
import com.drzhang.todo.data.TaskTab
import com.drzhang.todo.mode.TaskViewModel

@Composable
fun MainScreen(
    viewModel: TaskViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val tab by viewModel.currentTab.collectAsState()
    val taskState by viewModel.tasksState.collectAsStateWithLifecycle()

    val showEditor = viewModel.showEditor
    val editingTask = viewModel.editingTask

    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val iconSize = 24.dp

                FloatingActionButton(onClick = onToggleTheme) {
                    Icon(
                        painter = painterResource(
                            id = if (isDarkTheme) R.drawable.drak_mode else R.drawable.white_mode
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }

                FloatingActionButton(onClick = { viewModel.addTask() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TaskTabBar(tab, viewModel::switchTab)
            TaskList(tab, taskState.tasks, taskState.isLoading, viewModel)
        }

        if (showEditor) {
            EditTaskDialog(
                task = editingTask,
                onDismiss = viewModel::closeEditor,
                onSubmit = { title, priority, date ->
                    viewModel.saveTask(title, priority, date)
                }
            )
        }
    }
}

@Composable
fun TaskTabBar(current: TaskTab, onTabChange: (TaskTab) -> Unit) {
    TabRow(selectedTabIndex = current.ordinal) {
        TaskTab.entries.forEach { tab ->
            Tab(
                selected = tab == current,
                onClick = { onTabChange(tab) },
                text = { Text(tab.title) }
            )
        }
    }
}

@Composable
fun TaskList(
    tab: TaskTab,
    tasks: List<TaskEntity>,
    isLoading: Boolean,
    viewModel: TaskViewModel
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
        }
        return
    }

    val grouped = tasks.groupBy { it.taskDate }

    LazyColumn {
        grouped.forEach { (date, dayTasks) ->
            item {
                TaskDaySection(
                    date = date,
                    tasks = dayTasks,
                    tab = tab,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TaskDaySection(
    date: String,
    tasks: List<TaskEntity>,
    tab: TaskTab,
    viewModel: TaskViewModel
) {
    var expanded by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp)
        ) {
            Text(date, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        if (expanded) {
            tasks.forEach { task ->
                key(task.id) {
                    TaskItem(task, tab, viewModel)
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: TaskEntity,
    tab: TaskTab,
    viewModel: TaskViewModel
) {
    val priority = TaskPriority.from(task.priority)
    val canSwipeToComplete = tab == TaskTab.TODO
    val canSwipeToRestore = tab == TaskTab.DONE
    val canSwipeToDelete = true

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (canSwipeToDelete) {
                        viewModel.delete(task)
                        true
                    } else {
                        false
                    }
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    when {
                        canSwipeToComplete -> {
                            viewModel.markDone(task)
                            true
                        }

                        canSwipeToRestore -> {
                            viewModel.restore(task)
                            true
                        }

                        else -> false
                    }
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    Row(modifier = Modifier.clickable(task.status == 0) {
        viewModel.editTask(task)
    }) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = canSwipeToDelete,
            enableDismissFromEndToStart = canSwipeToComplete || canSwipeToRestore,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val icon = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                    SwipeToDismissBoxValue.EndToStart -> when {
                        canSwipeToComplete -> Icons.Default.Check
                        canSwipeToRestore -> Icons.Default.Build
                        else -> null
                    }

                    SwipeToDismissBoxValue.Settled -> null
                }
                val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = alignment
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = priority.color.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = task.content,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    }
}
