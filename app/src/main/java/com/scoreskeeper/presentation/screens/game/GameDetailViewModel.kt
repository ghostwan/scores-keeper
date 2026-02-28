package com.scoreskeeper.presentation.screens.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreskeeper.domain.model.Game
import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.model.PlayerStats
import com.scoreskeeper.domain.model.Session
import com.scoreskeeper.domain.repository.GameRepository
import com.scoreskeeper.domain.usecase.player.GetAllPlayersUseCase
import com.scoreskeeper.domain.usecase.session.CreateSessionUseCase
import com.scoreskeeper.domain.usecase.session.DeleteSessionUseCase
import com.scoreskeeper.domain.usecase.session.GetPlayerStatsUseCase
import com.scoreskeeper.domain.usecase.session.GetSessionsByGameUseCase
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
            val game = gameRepository.getGameById(gameId)
            _uiState.update { it.copy(game = game, isLoading = false) }
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
        val maxPlayers = _uiState.value.game?.maxPlayers ?: 10
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
            val sessionId = createSessionUseCase(gameId, players)
            _uiState.update { it.copy(showNewSessionSheet = false, newSessionId = sessionId) }
        }
    }

    fun onSessionNavigated() = _uiState.update { it.copy(newSessionId = null) }

    fun showDeleteSessionDialog(session: Session) =
        _uiState.update { it.copy(sessionToDelete = session) }

    fun hideDeleteSessionDialog() =
        _uiState.update { it.copy(sessionToDelete = null) }

    fun confirmDeleteSession() {
        val session = _uiState.value.sessionToDelete ?: return
        viewModelScope.launch {
            deleteSessionUseCase(session)
            _uiState.update { it.copy(sessionToDelete = null) }
        }
    }
}
