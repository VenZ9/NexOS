package dev.nexos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.nexos.domain.model.TaskbarAlignment
import dev.nexos.domain.model.WallpaperTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexos_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val WALLPAPER_THEME = stringPreferencesKey("wallpaper_theme")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val ACCENT_COLOR_HEX = longPreferencesKey("accent_color_hex")
        val TASKBAR_ALIGNMENT = stringPreferencesKey("taskbar_alignment")
        val AUTO_HIDE_TASKBAR = booleanPreferencesKey("auto_hide_taskbar")
        val SHOW_DESKTOP_ICONS = booleanPreferencesKey("show_desktop_icons")
        val ACTIVE_RUNTIME_TYPE = stringPreferencesKey("active_runtime_type")
        val STRICT_PE_VALIDATION = booleanPreferencesKey("strict_pe_validation")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val wallpaperName = preferences[PreferencesKeys.WALLPAPER_THEME] ?: WallpaperTheme.FLUENT_DARK.name
            val wallpaper = try {
                WallpaperTheme.valueOf(wallpaperName)
            } catch (e: Exception) {
                WallpaperTheme.FLUENT_DARK
            }

            val alignmentName = preferences[PreferencesKeys.TASKBAR_ALIGNMENT] ?: TaskbarAlignment.CENTER.name
            val alignment = try {
                TaskbarAlignment.valueOf(alignmentName)
            } catch (e: Exception) {
                TaskbarAlignment.CENTER
            }

            UserPreferences(
                wallpaperTheme = wallpaper,
                customWallpaperUri = preferences[PreferencesKeys.CUSTOM_WALLPAPER_URI] ?: "",
                accentColorHex = preferences[PreferencesKeys.ACCENT_COLOR_HEX] ?: 0xFF0078D4L,
                taskbarAlignment = alignment,
                autoHideTaskbar = preferences[PreferencesKeys.AUTO_HIDE_TASKBAR] ?: false,
                showDesktopIcons = preferences[PreferencesKeys.SHOW_DESKTOP_ICONS] ?: true,
                activeRuntimeType = preferences[PreferencesKeys.ACTIVE_RUNTIME_TYPE] ?: "REAL",
                strictPeValidation = preferences[PreferencesKeys.STRICT_PE_VALIDATION] ?: true
            )
        }

    suspend fun updateWallpaperTheme(theme: WallpaperTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WALLPAPER_THEME] = theme.name
        }
    }

    suspend fun updateCustomWallpaperUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_WALLPAPER_URI] = uri
            preferences[PreferencesKeys.WALLPAPER_THEME] = WallpaperTheme.CUSTOM_IMAGE.name
        }
    }

    suspend fun updateAccentColor(colorHex: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR_HEX] = colorHex
        }
    }

    suspend fun updateTaskbarAlignment(alignment: TaskbarAlignment) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TASKBAR_ALIGNMENT] = alignment.name
        }
    }

    suspend fun updateAutoHideTaskbar(autoHide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_HIDE_TASKBAR] = autoHide
        }
    }

    suspend fun updateShowDesktopIcons(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_DESKTOP_ICONS] = show
        }
    }

    suspend fun updateActiveRuntimeType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_RUNTIME_TYPE] = type
        }
    }

    suspend fun updateStrictPeValidation(strict: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STRICT_PE_VALIDATION] = strict
        }
    }
}
