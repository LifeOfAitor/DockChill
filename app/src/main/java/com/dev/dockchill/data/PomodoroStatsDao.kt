package com.dev.dockchill.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroStatsDao {

    @Query("SELECT * FROM pomodoro_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: Long): PomodoroStats?

    @Query("SELECT * FROM pomodoro_stats ORDER BY date DESC")
    fun getAllStats(): Flow<List<PomodoroStats>>

    @Query("SELECT SUM(completedPomodoros) FROM pomodoro_stats")
    fun getTotalPomodoros(): Flow<Int?>

    @Query("SELECT SUM(totalFocusMinutes) FROM pomodoro_stats")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: PomodoroStats)

    @Query("SELECT * FROM pomodoro_stats WHERE date >= :startDate ORDER BY date ASC")
    fun getStatsFromDate(startDate: Long): Flow<List<PomodoroStats>>
}