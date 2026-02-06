package com.drzhang.todo.data

import androidx.compose.ui.graphics.Color

enum class TaskTab(val title: String) {
    TODO("待办"),
    DONE("已完成"),
    ALL("全部")
}

enum class TaskPriority(val level: Int, val color: Color) {
    URGENT(0, Color(0xFFE53935)),
    NORMAL(1, Color(0xFFFB8C00)),
    PLAN(2, Color(0xFF43A047));

    companion object {
        fun from(level: Int) = TaskPriority.entries.first { it.level == level }
    }
}
