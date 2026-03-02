package com.ghostwan.scoreskeeper.presentation.screens.game

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostwan.scoreskeeper.domain.model.Game
import com.ghostwan.scoreskeeper.domain.model.Player
import com.ghostwan.scoreskeeper.domain.model.PlayerStats
import com.ghostwan.scoreskeeper.domain.model.Session
import com.ghostwan.scoreskeeper.domain.repository.GameRepository
import com.ghostwan.scoreskeeper.domain.usecase.player.GetAllPlayersUseCase
import com.ghostwan.scoreskeeper.domain.usecase.session.CreateSessionUseCase
import com.ghostwan.scoreskeeper.domain.usecase.session.DeleteSessionUseCase
import com.ghostwan.scoreskeeper.domain.usecase.session.GetPlayerStatsUseCase
import com.ghostwan.scoreskeeper.domain.usecase.session.GetSessionsByGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameDetailUiState(
    val game: Game? = null,
    val sessions: List<Session> = emptyList(),
    val stats: List<PlayerStats> = emptyList(),
    val allPlayers: List<Player> = emptyList(),
    val selectedPlayers: List<Player> = emptyList(),
    val isLoading: Boolean = true,
    val newSessionId: Long? = null,
    val showNewSessionSheet: Boolean = false,
    val sessionToDelete: Session? = null,
    val showIconPicker: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    getAllPlayersUseCase: GetAllPlayersUseCase,
    getSessionsByGameUseCase: GetSessionsByGameUseCase,
    getPlayerStatsUseCase: GetPlayerStatsUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
) : ViewModel() {

    private val gameId: Long = checkNotNull(savedStateHandle["gameId"])

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val game = gameRepository.getGameById(gameId)
                _uiState.update { it.copy(game = game, isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load game", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }

        combine(
            getSessionsByGameUseCase(gameId),
            getPlayerStatsUseCase(gameId),
            getAllPlayersUseCase(),
        ) { sessions, stats, players ->
            Triple(sessions, stats, players)
        }.onEach { (sessions, stats, players) ->
            _uiState.update {
                it.copy(sessions = sessions, stats = stats, allPlayers = players)
            }
        }.launchIn(viewModelScope)
    }

    fun showNewSessionSheet() = _uiState.update { it.copy(showNewSessionSheet = true) }
    fun hideNewSessionSheet() = _uiState.update {
        it.copy(showNewSessionSheet = false, selectedPlayers = emptyList())
    }

    fun togglePlayerSelection(player: Player) {
        val current = _uiState.value.selectedPlayers
        val maxPlayers = _uiState.value.game?.maxPlayers ?: Int.MAX_VALUE
        _uiState.update {
            it.copy(
                selectedPlayers = if (player in current) {
                    current - player
                } else if (current.size < maxPlayers) {
                    current + player
                } else current
            )
        }
    }

    fun createSession() {
        val players = _uiState.value.selectedPlayers
        val minPlayers = _uiState.value.game?.minPlayers ?: 2
        if (players.size < minPlayers) return
        viewModelScope.launch {
            try {
                val sessionId = createSessionUseCase(gameId, players)
                _uiState.update { it.copy(showNewSessionSheet = false, newSessionId = sessionId) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create session", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onSessionNavigated() = _uiState.update { it.copy(newSessionId = null) }

    fun showIconPicker() = _uiState.update { it.copy(showIconPicker = true) }
    fun hideIconPicker() = _uiState.update { it.copy(showIconPicker = false) }

    fun updateGameIcon(icon: String) {
        val game = _uiState.value.game ?: return
        val updatedGame = game.copy(icon = icon)
        _uiState.update { it.copy(game = updatedGame, showIconPicker = false) }
        viewModelScope.launch {
            try {
                gameRepository.updateGame(updatedGame)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update game icon", e)
                _uiState.update { it.copy(game = game, error = e.message) }
            }
        }
    }

    fun showDeleteSessionDialog(session: Session) =
        _uiState.update { it.copy(sessionToDelete = session) }

    fun hideDeleteSessionDialog() =
        _uiState.update { it.copy(sessionToDelete = null) }

    fun confirmDeleteSession() {
        val session = _uiState.value.sessionToDelete ?: return
        viewModelScope.launch {
            try {
                deleteSessionUseCase(session)
                _uiState.update { it.copy(sessionToDelete = null) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete session", e)
                _uiState.update { it.copy(sessionToDelete = null, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    companion object {
        private const val TAG = "GameDetailVM"
    }
}
