package org.edward.app.presentations.screens.main.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import org.edward.app.data.local.DataStoreRepository
import org.edward.app.presentations.screens.main.profile.settings.SettingsScreen
import org.koin.core.component.KoinComponent

class ProfileScreenModel(private val dataStoreRepository: DataStoreRepository) :
    ScreenModel, KoinComponent {

    fun handleNavigate(index: Int, navigator: Navigator) {
        when (index) {
            0 -> println("Click")
            1 -> println("Click")
            2 -> println("Click")
            3 -> navigator.parent?.push(SettingsScreen())
        }
    }

    fun clearDataStore(onSuccess: () -> Unit) {
        screenModelScope.launch {
            dataStoreRepository.clearToken()
            onSuccess()
        }
    }
}