package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val backendUrl: String = "",
    val useDirectGemini: Boolean = true,
    val customGeminiKey: String = "",
    val historyEnabled: Boolean = true,
    val textLimit: Int = 5000,
    val darkTheme: Boolean = true
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("humanize_ai_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            backendUrl = prefs.getString("backend_url", "") ?: "",
            useDirectGemini = prefs.getBoolean("use_direct_gemini", true),
            customGeminiKey = prefs.getString("custom_gemini_key", "") ?: "",
            historyEnabled = prefs.getBoolean("history_enabled", true),
            textLimit = prefs.getInt("text_limit", 5000),
            darkTheme = prefs.getBoolean("dark_theme", true)
        )
    }

    fun updateBackendUrl(url: String) {
        prefs.edit().putString("backend_url", url.trim()).apply()
        _settings.value = loadSettings()
    }

    fun updateUseDirectGemini(useDirect: Boolean) {
        prefs.edit().putBoolean("use_direct_gemini", useDirect).apply()
        _settings.value = loadSettings()
    }

    fun updateCustomGeminiKey(key: String) {
        prefs.edit().putString("custom_gemini_key", key.trim()).apply()
        _settings.value = loadSettings()
    }

    fun updateHistoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("history_enabled", enabled).apply()
        _settings.value = loadSettings()
    }

    fun updateTextLimit(limit: Int) {
        prefs.edit().putInt("text_limit", limit).apply()
        _settings.value = loadSettings()
    }

    fun updateDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
        _settings.value = loadSettings()
    }
}
