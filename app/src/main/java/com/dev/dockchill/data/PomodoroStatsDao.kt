package com.dev.dockchill.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Aurretik sortuta daukagun taularako kontsultak prestatuko dira hemen.
// SELECT, INSERT eta DELETE aukerak izango ditugu.
@Dao
interface PomodoroStatsDao {

    //Lortuko ditugu data espezifiko baterako estadistikak
    @Query("SELECT * FROM pomodoro_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: Long): PomodoroStats?

    // Estadistikak lortuko ditugu berrienetik zaharrenera
    @Query("SELECT * FROM pomodoro_stats ORDER BY date DESC")
    fun getAllStats(): Flow<List<PomodoroStats>>

    //Lortuko ditugu guztira bete ditugun sesioak
    @Query("SELECT SUM(completedPomodoros) FROM pomodoro_stats")
    fun getTotalPomodoros(): Flow<Int?>

    // Lortuko ditugu guztira izan ditugun kontzentrazio minutu guztiak
    @Query("SELECT SUM(totalFocusMinutes) FROM pomodoro_stats")
    fun getTotalFocusMinutes(): Flow<Int?>

    // Sartuko ditugu estadistika berriak taulan
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: PomodoroStats)

    // Lortuko ditugu data espezifiko baterako estadistikak
    @Query("SELECT * FROM pomodoro_stats WHERE date >= :startDate ORDER BY date ASC")
    fun getStatsFromDate(startDate: Long): Flow<List<PomodoroStats>>
}