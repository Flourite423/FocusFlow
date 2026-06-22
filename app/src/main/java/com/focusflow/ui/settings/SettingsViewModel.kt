package com.focusflow.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    data class UiState(
        val themeMode: Int = 0, // 0: system, 1: light, 2: dark
        val dailyGoalMinutes: Int = 60,
        val enableNotifications: Boolean = true,
        val freezeLimit: Int = 2,
        val language: String = "zh_CN"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // TODO: Load settings from DataStore
    }
}
