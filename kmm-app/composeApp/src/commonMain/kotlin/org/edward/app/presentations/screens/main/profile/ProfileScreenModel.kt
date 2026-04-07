package org.edward.app.presentations.screens.main.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.edward.app.data.local.DataStoreRepository
import org.edward.app.data.remote.auth.AuthRepository

class ProfileScreenModel(
    private val dataStoreRepository: DataStoreRepository,
    private val authRepository: AuthRepository,
) : ScreenModel {

    data class ProfileUiState(
        val userName: String = "",
        val userEmail: String = "",
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        screenModelScope.launch {
            dataStoreRepository.getUserName().collectLatest { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
        }
        screenModelScope.launch {
            dataStoreRepository.getUserEmail().collectLatest { email ->
                _uiState.value = _uiState.value.copy(userEmail = email)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        screenModelScope.launch {
            authRepository.logout()
            dataStoreRepository.clearToken()
            dataStoreRepository.clearUserInfo()
            onComplete()
        }
    }
}
