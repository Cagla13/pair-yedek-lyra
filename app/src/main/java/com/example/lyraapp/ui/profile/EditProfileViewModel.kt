package com.example.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<EditProfileEffect>(Channel.BUFFERED)
    val effect: Flow<EditProfileEffect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRepository.fetchCurrentUser()
            val user = authRepository.currentUser.first()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    firstName = user?.firstName.orEmpty(),
                    lastName = user?.lastName.orEmpty(),
                    birthDate = user?.birthDate.orEmpty(),
                    isSaveEnabled = false,
                )
            }
        }
    }

    fun onIntent(intent: EditProfileIntent) {
        when (intent) {
            is EditProfileIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is EditProfileIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is EditProfileIntent.BirthDateChanged -> updateForm { it.copy(birthDate = intent.value) }
            EditProfileIntent.Save -> save()
            EditProfileIntent.Back -> viewModelScope.launch { _effect.send(EditProfileEffect.NavigateBack) }
        }
    }

    private fun updateForm(transform: (EditProfileUiState) -> EditProfileUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isSaveEnabled = updated.isFormValid())
        }
    }

    private fun save() {
        val state = _uiState.value
        if (!state.isSaveEnabled || state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            authRepository.completeProfile(
                firstName = state.firstName,
                lastName = state.lastName,
                birthDate = state.birthDate,
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _effect.send(EditProfileEffect.NavigateBack)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false) }
                _effect.send(EditProfileEffect.ShowError(error.message ?: "Profil güncellenemedi."))
            }
        }
    }
}

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveEnabled: Boolean = false,
) {
    fun isFormValid(): Boolean =
        firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            birthDate.matches(BIRTH_DATE_REGEX)
}

sealed interface EditProfileIntent {
    data class FirstNameChanged(val value: String) : EditProfileIntent
    data class LastNameChanged(val value: String) : EditProfileIntent
    data class BirthDateChanged(val value: String) : EditProfileIntent
    data object Save : EditProfileIntent
    data object Back : EditProfileIntent
}

sealed interface EditProfileEffect {
    data object NavigateBack : EditProfileEffect
    data class ShowError(val message: String) : EditProfileEffect
}

private val BIRTH_DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
