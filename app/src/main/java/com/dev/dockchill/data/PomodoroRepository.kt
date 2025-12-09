package com.dev.dockchill.data

import com.dev.dockchill.data.PomodoroStatsDao
import com.dev.dockchill.data.PomodoroStats
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PomodoroRepository(private val pomodoroStatsDao: PomodoroStatsDao) {

    val allStats: Flow<List<PomodoroStats>> = pomodoroStatsDao.getAllStats()
    val totalPomodoros: Flow<Int?> = pomodoroStatsDao.getTotalPomodoros()
    val totalFocusMinutes: Flow<Int?> = pomodoroStatsDao.getTotalFocusMinutes()

    suspend fun addCompletedPomodoro(focusMinutes: Int) {
        val today = getTodayDateInMillis()
        val existingStats = pomodoroStatsDao.getStatsByDate(today)

        if (existingStats != null) {
            pomodoroStatsDao.insertOrUpdate(
                existingStats.copy(
                    completedPomodoros = existingStats.completedPomodoros + 1,
                    totalFocusMinutes = existingStats.totalFocusMinutes + focusMinutes
                )
            )
        } else {
            pomodoroStatsDao.insertOrUpdate(
                PomodoroStats(
                    date = today,
                    completedPomodoros = 1,
                    totalFocusMinutes = focusMinutes
                )
            )
        }
    }

    suspend fun getTodayStats(): PomodoroStats? {
        return pomodoroStatsDao.getStatsByDate(getTodayDateInMillis())
    }

    suspend fun calculateCurrentStreak(): Int {
        val allStats = pomodoroStatsDao.getAllStats()
        // This will be calculated in ViewModel from Flow
        return 0
    }

    private fun getTodayDateInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}