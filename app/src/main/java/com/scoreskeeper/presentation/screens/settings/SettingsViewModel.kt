package com.scoreskeeper.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreskeeper.data.backup.BackupResult
import com.scoreskeeper.data.backup.SyncManager
import com.scoreskeeper.data.backup.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncManager: SyncManager,
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncManager.syncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onSignedIn(email: String, displayName: String) {
        viewModelScope.launch {
            syncManager.enableSync(email, displayName)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            syncManager.disableSync()
        }
    }

    fun manualBackup() {
        viewModelScope.launch {
            val result = syncManager.manualBackup()
            _message.value = when (result) {
                is BackupResult.Success -> "Sauvegarde envoyée sur Google Drive"
                is BackupResult.Error -> "Erreur : ${result.message}"
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            val result = syncManager.restoreFromBackup()
            _message.value = when (result) {
                is BackupResult.Success -> "Données restaurées ! Redémarrez l'application."
                is BackupResult.Error -> "Erreur : ${result.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
