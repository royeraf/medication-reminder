package com.medicationreminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeSetting {
    SYSTEM,
    LIGHT,
    DARK
}

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_setting")
    }

    val themeSetting = context.dataStore.data.map {
        ThemeSetting.valueOf(it[Keys.THEME] ?: ThemeSetting.SYSTEM.name)
    }

    suspend fun setThemeSetting(theme: ThemeSetting) {
        context.dataStore.edit { 
            it[Keys.THEME] = theme.name 
        }
    }
}
