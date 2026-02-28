package com.ghostwan.scoreskeeper.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

enum class AiProvider(val label: String, val urlTemplate: String) {
    CHATGPT("ChatGPT", "https://chatgpt.com/?q=%s"),
    GEMINI("Gemini", "https://gemini.google.com/app?q=%s"),
    CLAUDE("Claude", "https://claude.ai/new?q=%s"),
    COPILOT("Copilot", "https://copilot.microsoft.com/?q=%s"),
    MISTRAL("Mistral (Le Chat)", "https://chat.mistral.ai/chat?q=%s"),
}

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_CHART_AREA_FILL = booleanPreferencesKey("chart_area_fill")
        private val KEY_CHART_START_FROM_ZERO = booleanPreferencesKey("chart_start_from_zero")
        private val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
    }

    val chartAreaFill: Flow<Boolean> = context.appPreferencesDataStore.data
        .map { prefs -> prefs[KEY_CHART_AREA_FILL] ?: true }

    val chartStartFromZero: Flow<Boolean> = context.appPreferencesDataStore.data
        .map { prefs -> prefs[KEY_CHART_START_FROM_ZERO] ?: false }

    val aiProvider: Flow<AiProvider> = context.appPreferencesDataStore.data
        .map { prefs ->
            val name = prefs[KEY_AI_PROVIDER] ?: AiProvider.CHATGPT.name
            AiProvider.entries.firstOrNull { it.name == name } ?: AiProvider.CHATGPT
        }

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

    suspend fun setAiProvider(provider: AiProvider) {
        context.appPreferencesDataStore.edit { prefs ->
            prefs[KEY_AI_PROVIDER] = provider.name
        }
    }
}
