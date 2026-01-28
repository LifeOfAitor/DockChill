package com.dev.dockchill

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Tema kudeatzen duen klase laguntzailea.
 * SharedPreferences erabiliz tema erabiltzailearen hautaketa gordetzeko.
 */
object ThemeManager {

    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_THEME = "theme_mode"

    // Tema moduak
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    /**
     * SharedPreferences eskuratzeko metodoa
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Gordetako tema modua lortzeko
     */
    fun getSavedThemeMode(context: Context): Int {
        return getPreferences(context).getInt(KEY_THEME, THEME_SYSTEM)
    }

    /**
     * Tema modua gordetzeko
     */
    fun saveThemeMode(context: Context, mode: Int) {
        getPreferences(context).edit().putInt(KEY_THEME, mode).apply()
    }

    /**
     * Tema aplikatzeko gordetako preferentzien arabera
     */
    fun applyTheme(mode: Int) {
        when (mode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Uneko tema modua lortzeko (aplikatuta dagoena)
     */
    fun getCurrentThemeMode(): Int {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> THEME_LIGHT
            AppCompatDelegate.MODE_NIGHT_YES -> THEME_DARK
            else -> THEME_SYSTEM
        }
    }

    /**
     * Tema aldatzeko (argia <-> iluna)
     */
    fun toggleTheme(context: Context) {
        val currentMode = getCurrentThemeMode()
        val newMode = if (currentMode == THEME_DARK) THEME_LIGHT else THEME_DARK
        saveThemeMode(context, newMode)
        applyTheme(newMode)
    }

    /**
     * Ea unean ilun moduan gauden jakiteko
     */
    fun isDarkModeActive(): Boolean {
        return getCurrentThemeMode() == THEME_DARK
    }
}
