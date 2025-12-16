package com.dev.dockchill.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// PomodoroStats taula sortuko dugu @Entity jarriz, bertan hainbat datu gordeko ditugu
// id, data, bukatu ditugun sesioak eta kontzentrazio minutuak
@Entity(tableName = "pomodoro_stats")
data class PomodoroStats(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // Date in milliseconds
    val completedPomodoros: Int = 0,
    val totalFocusMinutes: Int = 0
)