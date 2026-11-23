package com.example.studenttaskmanager.data

import androidx.room3.ColumnTypeConverter

class Converters {
    @ColumnTypeConverter fun fromStatus(s: TaskStatus): String = s.name
    @ColumnTypeConverter fun toStatus(v: String): TaskStatus = TaskStatus.valueOf(v)
}