package com.focusflow.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val THEME_KEY = intPreferencesKey("theme_mode")
    private val GOAL_KEY = intPreferencesKey("daily_goal_minutes")
    private val NOTIFY_KEY = booleanPreferencesKey("enable_notifications")
    private val FREEZE_KEY = intPreferencesKey("freeze_limit")

    data class UiState(
        val themeMode: Int = 0,
        val dailyGoalMinutes: Int = 60,
        val enableNotifications: Boolean = true,
        val freezeLimit: Int = 2
    )

    val uiState: StateFlow<UiState> = dataStore.data.map { prefs ->
        UiState(
            themeMode = prefs[THEME_KEY] ?: 0,
            dailyGoalMinutes = prefs[GOAL_KEY] ?: 60,
            enableNotifications = prefs[NOTIFY_KEY] ?: true,
            freezeLimit = prefs[FREEZE_KEY] ?: 2
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setThemeMode(mode: Int) {
        viewModelScope.launch { dataStore.edit { it[THEME_KEY] = mode } }
    }

    fun setDailyGoal(minutes: Int) {
        viewModelScope.launch { dataStore.edit { it[GOAL_KEY] = minutes } }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[NOTIFY_KEY] = enabled } }
    }

    fun setFreezeLimit(limit: Int) {
        viewModelScope.launch { dataStore.edit { it[FREEZE_KEY] = limit } }
    }
}
