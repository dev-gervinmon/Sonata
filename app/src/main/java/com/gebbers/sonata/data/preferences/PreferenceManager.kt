package com.gebbers.sonata.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferenceManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val DYNAMIC_THEMING = booleanPreferencesKey("dynamic_theming")
        val DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        val FADE_DURATION = longPreferencesKey("fade_duration")
    }

    val dynamicTheming: Flow<Boolean> = dataStore.data.map { it[DYNAMIC_THEMING] ?: true }
    val darkMode: Flow<String> = dataStore.data.map { it[DARK_MODE] ?: "system" }
    val fadeDuration: Flow<Long> = dataStore.data.map { it[FADE_DURATION] ?: 1000L }

    suspend fun setDynamicTheming(enabled: Boolean) {
        dataStore.edit { it[DYNAMIC_THEMING] = enabled }
    }

    suspend fun setDarkMode(mode: String) {
        dataStore.edit { it[DARK_MODE] = mode }
    }

    suspend fun setFadeDuration(duration: Long) {
        dataStore.edit { it[FADE_DURATION] = duration }
    }
}
