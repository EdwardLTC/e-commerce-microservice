package org.edward.app.presentations.screens.main.profile.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.edward.app.data.local.DataStoreRepository
import org.koin.core.component.KoinComponent

class SettingsScreenModel(private val dataStoreRepository: DataStoreRepository) :
    ScreenModel, KoinComponent {

    private val _isDarkTheme = MutableStateFlow(false)
    private val _navType = MutableStateFlow(DataStoreRepository.Companion.NavType.Bottom)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme
    val navType: StateFlow<DataStoreRepository.Companion.NavType> = _navType

    init {
        screenModelScope.launch {
            dataStoreRepository.isDarkTheme().collectLatest { dark ->
                _isDarkTheme.value = dark
            }
        }

        screenModelScope.launch {
            dataStoreRepository.getNavType().collectLatest { type ->
                _navType.value = type
            }
        }
    }

    fun toggleTheme() {
        screenModelScope.launch {
            val newValue = !_isDarkTheme.value
            dataStoreRepository.saveDarkTheme(newValue)
        }
    }

    fun changeNavType(type: DataStoreRepository.Companion.NavType) {
        screenModelScope.launch {
            dataStoreRepository.saveNavType(type)
        }
    }
}