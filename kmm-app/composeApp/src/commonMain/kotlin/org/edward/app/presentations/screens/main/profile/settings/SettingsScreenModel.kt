package org.edward.app.presentations.screens.main.profile.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.edward.app.data.local.DataStoreRepository

class SettingsScreenModel(private val dataStoreRepository: DataStoreRepository) : ScreenModel {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    init {
        screenModelScope.launch {
            dataStoreRepository.isDarkTheme().collectLatest { dark ->
                _isDarkTheme.value = dark
            }
        }
    }

    fun toggleTheme() {
        screenModelScope.launch {
            val newValue = !_isDarkTheme.value
            dataStoreRepository.saveDarkTheme(newValue)
        }
    }
}
