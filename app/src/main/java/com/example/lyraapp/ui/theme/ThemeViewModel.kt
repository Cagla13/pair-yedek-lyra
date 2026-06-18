package com.example.lyraapp.ui.theme

import androidx.lifecycle.ViewModel
import com.example.lyraapp.data.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = themeRepository.isDarkTheme

    fun setDarkTheme(isDark: Boolean) {
        themeRepository.setDarkTheme(isDark)
    }
}
