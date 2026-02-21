package com.medicationreminder.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationreminder.data.LanguageSetting
import com.medicationreminder.data.SettingsDataStore
import com.medicationreminder.data.ThemeSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val themeSetting = settingsDataStore.themeSetting.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeSetting.SYSTEM
    )

    val languageSetting = settingsDataStore.languageSetting.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LanguageSetting.SYSTEM
    )

    fun setThemeSetting(theme: ThemeSetting) {
        viewModelScope.launch {
            settingsDataStore.setThemeSetting(theme)
        }
    }

    fun setLanguageSetting(language: LanguageSetting) {
        viewModelScope.launch {
            settingsDataStore.setLanguageSetting(language)
        }
    }
}
