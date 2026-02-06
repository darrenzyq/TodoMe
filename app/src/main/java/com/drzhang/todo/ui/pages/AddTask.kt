package com.drzhang.todo.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drzhang.todo.data.TaskEntity
import com.drzhang.todo.data.TaskPriority
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: TaskEntity?,
    onDismiss: () -> Unit,
    onSubmit: (String, Int, String) -> Unit
) {

    val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    var showDatePicker by remember { mutableStateOf(false) }

    var content by remember(task) {
        mutableStateOf(task?.content ?: "")
    }
    var priority by remember(task) {
        mutableIntStateOf(task?.priority ?: TaskPriority.NORMAL.level)
    }
    var taskDate by remember(task) {
        mutableStateOf(task?.taskDate ?: LocalDate.now().toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onSubmit(content, priority, taskDate) },
                enabled = content.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = {
            Text("添加任务")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("任务内容") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                DateSelectRow(
                    date = taskDate,
                    onClick = { showDatePicker = true }
                )

                Spacer(Modifier.height(16.dp))

                Text("优先级", style = MaterialTheme.typography.labelMedium)

                Spacer(Modifier.height(8.dp))

                PrioritySelector(
                    selected = TaskPriority.from(priority),
                    onSelect = { priority = it.level }
                )
            }
        }
    )

    if (showDatePicker) {
        TaskDatePicker(
            selectedDate = LocalDate.parse(taskDate, DATE_FORMATTER),
            onDateSelected = {
                taskDate = it.format(DATE_FORMATTER)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun DateSelectRow(
    date: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis =
            selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
fun PrioritySelector(
    selected: TaskPriority,
    onSelect: (TaskPriority) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskPriority.entries.forEach { priority ->
            FilterChip(
                modifier = Modifier.fillMaxWidth(),
                selected = priority == selected,
                onClick = { onSelect(priority) },
                label = { Text(priority.name) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(priority.color, CircleShape)
                    )
                }
            )
        }
    }
}

