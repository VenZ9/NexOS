package dev.nexos.apps.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nexos.data.preferences.UserPreferences
import dev.nexos.data.preferences.UserPreferencesRepository
import dev.nexos.domain.model.TaskbarAlignment
import dev.nexos.domain.model.WallpaperTheme
import dev.nexos.runtime.RuntimeProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val runtimeProvider: RuntimeProvider
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setWallpaperTheme(theme: WallpaperTheme) {
        viewModelScope.launch {
            preferencesRepository.updateWallpaperTheme(theme)
        }
    }

    fun setCustomWallpaperUri(uri: String) {
        viewModelScope.launch {
            preferencesRepository.updateCustomWallpaperUri(uri)
        }
    }

    fun setAccentColor(colorHex: Long) {
        viewModelScope.launch {
            preferencesRepository.updateAccentColor(colorHex)
        }
    }

    fun setTaskbarAlignment(alignment: TaskbarAlignment) {
        viewModelScope.launch {
            preferencesRepository.updateTaskbarAlignment(alignment)
        }
    }

    fun setAutoHideTaskbar(autoHide: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoHideTaskbar(autoHide)
        }
    }

    fun setShowDesktopIcons(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowDesktopIcons(show)
        }
    }

    fun setActiveRuntimeType(type: String) {
        viewModelScope.launch {
            preferencesRepository.updateActiveRuntimeType(type)
            runtimeProvider.setRuntimeType(type)
        }
    }

    fun setStrictPeValidation(strict: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateStrictPeValidation(strict)
        }
    }
}
