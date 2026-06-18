package com.example.lyraapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyraapp.data.theme.DEFAULT_DARK_THEME
import com.example.lyraapp.data.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = themeRepository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DEFAULT_DARK_THEME,
        )

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeRepository.setDarkTheme(isDark)
        }
    }
}
