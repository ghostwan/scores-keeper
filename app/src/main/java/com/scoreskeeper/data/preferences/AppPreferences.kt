package com.scoreskeeper.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_CHART_AREA_FILL = booleanPreferencesKey("chart_area_fill")
        private val KEY_CHART_START_FROM_ZERO = booleanPreferencesKey("chart_start_from_zero")
    }

    val chartAreaFill: Flow<Boolean> = context.appPreferencesDataStore.data
        .map { prefs -> prefs[KEY_CHART_AREA_FILL] ?: true }

    val chartStartFromZero: Flow<Boolean> = context.appPreferencesDataStore.data
        .map { prefs -> prefs[KEY_CHART_START_FROM_ZERO] ?: false }

    suspend fun setChartAreaFill(enabled: Boolean) {
        context.appPreferencesDataStore.edit { prefs ->
            prefs[KEY_CHART_AREA_FILL] = enabled
        }
    }

    suspend fun setChartStartFromZero(enabled: Boolean) {
        context.appPreferencesDataStore.edit { prefs ->
            prefs[KEY_CHART_START_FROM_ZERO] = enabled
        }
    }
}
