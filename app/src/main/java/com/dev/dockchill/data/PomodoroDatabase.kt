package com.dev.dockchill.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Datu basea hemen sortuko dugu.
// Fitxategi honetan lortuko dugu aurretik sortutako @Entity eta Dao fitxategiak elkarrekin lotzea
@Database(entities = [PomodoroStats::class], version = 1, exportSchema = false)
abstract class PomodoroDatabase : RoomDatabase() {

    abstract fun pomodoroStatsDao(): PomodoroStatsDao

    companion object {
        @Volatile
        private var INSTANCE: PomodoroDatabase? = null

        fun getDatabase(context: Context): PomodoroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PomodoroDatabase::class.java,
                    "pomodoro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}