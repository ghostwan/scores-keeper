package com.ghostwan.scoreskeeper.presentation.screens.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.data.backup.BackupResult
import com.ghostwan.scoreskeeper.data.backup.GoogleAuthHelper
import com.ghostwan.scoreskeeper.data.backup.SyncManager
import com.ghostwan.scoreskeeper.data.backup.SyncState
import com.ghostwan.scoreskeeper.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLanguage(
    val code: String,
    val displayName: String,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val googleAuthHelper: GoogleAuthHelper,
    private val appPreferences: AppPreferences,
    private val application: Application,
) : ViewModel() {

    val availableLanguages = listOf(
        AppLanguage("", application.getString(R.string.language_system)),
        AppLanguage("en", "English"),
        AppLanguage("fr", "Français"),
        AppLanguage("es", "Español"),
        AppLanguage("de", "Deutsch"),
    )

    private val _currentLanguageCode = MutableStateFlow(getCurrentLanguageCode())
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState())

    val chartAreaFill: StateFlow<Boolean> = appPreferences.chartAreaFill
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val chartStartFromZero: StateFlow<Boolean> = appPreferences.chartStartFromZero
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun launchSignIn(launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>) {
        launcher.launch(googleAuthHelper.getSignInIntent())
    }

    fun onSignedIn(email: String, displayName: String) {
        viewModelScope.launch {
            syncManager.enableSync(email, displayName)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            googleAuthHelper.signOut {
                viewModelScope.launch {
                    syncManager.disableSync()
                }
            }
        }
    }

    fun toggleChartAreaFill(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setChartAreaFill(enabled)
        }
    }

    fun toggleChartStartFromZero(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setChartStartFromZero(enabled)
        }
    }

    fun manualBackup() {
        viewModelScope.launch {
            val result = syncManager.manualBackup()
            _message.value = when (result) {
                is BackupResult.Success -> application.getString(R.string.backup_success)
                is BackupResult.Error -> application.getString(R.string.error_prefix, result.message)
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            val result = syncManager.restoreFromBackup()
            _message.value = when (result) {
                is BackupResult.Success -> application.getString(R.string.restore_success)
                is BackupResult.Error -> application.getString(R.string.error_prefix, result.message)
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun changeLanguage(languageCode: String) {
        val localeList = if (languageCode.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
        _currentLanguageCode.value = languageCode
    }

    private fun getCurrentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "" else locales.get(0)?.language ?: ""
    }
}
