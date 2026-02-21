package com.medicationreminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeSetting {
    SYSTEM,
    LIGHT,
    DARK
}

enum class LanguageSetting(val code: String) {
    SYSTEM(""),
    ENGLISH("en"),
    SPANISH("es")
}

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_setting")
        val LANGUAGE = stringPreferencesKey("language_setting")
    }

    val themeSetting = context.dataStore.data.map {
        try {
            ThemeSetting.valueOf(it[Keys.THEME] ?: ThemeSetting.SYSTEM.name)
        } catch (e: Exception) {
            ThemeSetting.SYSTEM
        }
    }

    val languageSetting = context.dataStore.data.map {
        try {
            LanguageSetting.valueOf(it[Keys.LANGUAGE] ?: LanguageSetting.SYSTEM.name)
        } catch (e: Exception) {
            LanguageSetting.SYSTEM
        }
    }

    suspend fun setThemeSetting(theme: ThemeSetting) {
        context.dataStore.edit { 
            it[Keys.THEME] = theme.name 
        }
    }

    suspend fun setLanguageSetting(language: LanguageSetting) {
        context.dataStore.edit {
            it[Keys.LANGUAGE] = language.name
        }
    }
}
