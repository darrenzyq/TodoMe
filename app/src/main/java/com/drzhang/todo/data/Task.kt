package com.drzhang.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // yyyy-MM-dd
    val taskDate: String,

    val content: String,

    // 0 = todo, 1 = done
    val status: Int,

    // 0 = 紧急, 1 = 一般, 2 = 规划
    val priority: Int,

    val createTime: Long = System.currentTimeMillis()
)

enum class TaskStatus(val code: Int) {
    TODO(0),
    DONE(1)
}