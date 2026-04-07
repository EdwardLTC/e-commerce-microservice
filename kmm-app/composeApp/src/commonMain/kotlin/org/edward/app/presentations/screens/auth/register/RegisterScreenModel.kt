package org.edward.app.presentations.screens.auth.register

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.edward.app.data.local.DataStoreRepository
import org.edward.app.data.remote.auth.AuthRepository
import org.edward.app.data.remote.auth.LoginRequest
import org.edward.app.data.remote.auth.RegisterRequest
import org.edward.app.data.utils.AsyncResult

class RegisterScreenModel(
    private val dataStoreRepository: DataStoreRepository,
    private val authRepository: AuthRepository,
) : ScreenModel {
    data class RegisterUiState(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, error = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null)
    }

    fun register(onSuccess: () -> Unit) {
        screenModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null)

            if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
                _uiState.value = state.copy(isLoading = false, error = "All fields are required")
                return@launch
            }
            if (state.password.length < 6) {
                _uiState.value = state.copy(isLoading = false, error = "Password must be at least 6 characters")
                return@launch
            }
            if (state.password != state.confirmPassword) {
                _uiState.value = state.copy(isLoading = false, error = "Passwords do not match")
                return@launch
            }

            when (val result = authRepository.register(
                RegisterRequest(name = state.name, email = state.email, password = state.password)
            )) {
                is AsyncResult.Success -> {
                    dataStoreRepository.saveUserName(result.data.name)
                    dataStoreRepository.saveUserEmail(result.data.email)

                    when (val loginResult = authRepository.login(
                        LoginRequest(email = state.email, password = state.password)
                    )) {
                        is AsyncResult.Success -> {
                            dataStoreRepository.saveAccessToken(loginResult.data.accessToken, 3600 * 24 * 7)
                            dataStoreRepository.saveRefreshToken(loginResult.data.accessToken, 3600 * 24 * 30)
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            onSuccess()
                        }

                        is AsyncResult.Error -> {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            onSuccess()
                        }
                    }
                }

                is AsyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.displayMessage ?: "Registration failed"
                    )
                }
            }
        }
    }
}
