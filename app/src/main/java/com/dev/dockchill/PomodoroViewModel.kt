package com.dev.dockchill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dev.dockchill.data.PomodoroStats
import com.dev.dockchill.data.PomodoroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

// Hemendik lortuko dugu UI eta repository elkarrekin lotzea.
// Datuak modu konkurrenteak eguneratzeko erabiliko dugu klase hau MVVM printzipioa erabiliko duena
class PomodoroViewModel(private val repository: PomodoroRepository) : ViewModel() {

    val totalPomodoros = repository.totalPomodoros.asLiveData()
    val totalFocusMinutes = repository.totalFocusMinutes.asLiveData()

    private val _todayPomodoros = MutableStateFlow(0)
    val todayPomodoros: StateFlow<Int> = _todayPomodoros

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            // Gaurko pomodoroak kargatuko ditugu
            val todayStats = repository.getTodayStats()
            _todayPomodoros.value = todayStats?.completedPomodoros ?: 0

            // Zenbat sesio segidan egin ditugun kargatuko da
            repository.allStats.collect { statsList ->
                calculateStreaks(statsList)
            }
        }
    }

    // Pomodoro bat gehituko dugu datu basera
    fun addCompletedPomodoro(focusMinutes: Int) {
        viewModelScope.launch {
            repository.addCompletedPomodoro(focusMinutes)
            // Refresh today's count
            val todayStats = repository.getTodayStats()
            _todayPomodoros.value = todayStats?.completedPomodoros ?: 0
        }
    }

    // Zenbat sesio segidan egin ditugun kargatuko da
    private fun calculateStreaks(statsList: List<PomodoroStats>) {
        if (statsList.isEmpty()) {
            _currentStreak.value = 0
            _longestStreak.value = 0
            return
        }

        val sortedStats = statsList.sortedByDescending { it.date }
        val today = getTodayDateInMillis()

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var expectedDate = today

        for (stat in sortedStats) {
            if (stat.completedPomodoros > 0) {
                if (stat.date == expectedDate) {
                    tempStreak++
                    if (expectedDate == today || (currentStreak > 0 && expectedDate == today - TimeUnit.DAYS.toMillis(1))) {
                        currentStreak = tempStreak
                    }
                    expectedDate -= TimeUnit.DAYS.toMillis(1)
                } else {
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak
                    }
                    tempStreak = 1
                    expectedDate = stat.date - TimeUnit.DAYS.toMillis(1)
                }
            }
        }

        if (tempStreak > longestStreak) {
            longestStreak = tempStreak
        }

        _currentStreak.value = currentStreak
        _longestStreak.value = longestStreak
    }

    // gaurko data milisegundoetan lortuko dugu hemendik
    private fun getTodayDateInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

// ViewModelFactory klasea ViewModel-a sortzeko erabiliko dugu
class PomodoroViewModelFactory(private val repository: PomodoroRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PomodoroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}