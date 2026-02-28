package com.scoreskeeper.presentation.screens.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreskeeper.domain.model.RoundScore
import com.scoreskeeper.domain.model.SessionDetail
import com.scoreskeeper.domain.usecase.session.AddRoundScoreUseCase
import com.scoreskeeper.domain.usecase.session.DeleteRoundUseCase
import com.scoreskeeper.domain.usecase.session.FinishSessionUseCase
import com.scoreskeeper.domain.usecase.session.GetSessionDetailUseCase
import com.scoreskeeper.domain.usecase.session.UpdateRoundScoresUseCase
import com.scoreskeeper.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val detail: SessionDetail? = null,
    val isLoading: Boolean = true,
    // Map of playerId -> input string for current round
    val roundInputs: Map<Long, String> = emptyMap(),
    val showFinishDialog: Boolean = false,
    val showScoreEntry: Boolean = false,
    // Edit round state
    val editingRound: Int? = null,
    // Delete round state
    val roundToDelete: Int? = null,
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSessionDetailUseCase: GetSessionDetailUseCase,
    private val addRoundScoreUseCase: AddRoundScoreUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
    private val deleteRoundUseCase: DeleteRoundUseCase,
    private val updateRoundScoresUseCase: UpdateRoundScoresUseCase,
    appPreferences: AppPreferences,
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    val chartAreaFill: StateFlow<Boolean> = appPreferences.chartAreaFill
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        getSessionDetailUseCase(sessionId)
            .onEach { detail ->
                _uiState.update { state ->
                    state.copy(
                        detail = detail,
                        isLoading = false,
                        roundInputs = if (state.roundInputs.isEmpty()) {
                            detail?.players?.associate { it.id to "" } ?: emptyMap()
                        } else state.roundInputs,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onScoreInput(playerId: Long, value: String) {
        // Allow negative numbers and empty input
        if (value.isEmpty() || value == "-" || value.toIntOrNull() != null) {
            _uiState.update { it.copy(roundInputs = it.roundInputs + (playerId to value)) }
        }
    }

    fun showScoreEntry() = _uiState.update { state ->
        state.copy(
            showScoreEntry = true,
            editingRound = null,
            roundInputs = state.detail?.players?.associate { it.id to "" } ?: emptyMap(),
        )
    }

    fun hideScoreEntry() = _uiState.update { it.copy(showScoreEntry = false, editingRound = null) }

    fun submitRound() {
        val state = _uiState.value
        val detail = state.detail ?: return
        val round = detail.currentRound
        val scores = state.roundInputs

        // Validate all players have a score
        if (scores.any { (_, v) -> v.toIntOrNull() == null }) return

        viewModelScope.launch {
            scores.forEach { (playerId, value) ->
                addRoundScoreUseCase(
                    RoundScore(
                        sessionId = sessionId,
                        playerId = playerId,
                        round = round,
                        points = value.toInt(),
                    )
                )
            }
            _uiState.update {
                it.copy(
                    showScoreEntry = false,
                    roundInputs = detail.players.associate { p -> p.id to "" },
                )
            }
        }
    }

    // ---- Edit round ----

    fun editRound(round: Int) {
        val detail = _uiState.value.detail ?: return
        val roundScores = detail.rounds.filter { it.round == round }
        val inputs = detail.players.associate { player ->
            val score = roundScores.find { it.playerId == player.id }
            player.id to (score?.points?.toString() ?: "")
        }
        _uiState.update {
            it.copy(
                showScoreEntry = true,
                editingRound = round,
                roundInputs = inputs,
            )
        }
    }

    fun submitEditRound() {
        val state = _uiState.value
        val detail = state.detail ?: return
        val editingRound = state.editingRound ?: return
        val scores = state.roundInputs

        // Validate all players have a score
        if (scores.any { (_, v) -> v.toIntOrNull() == null }) return

        val existingScores = detail.rounds.filter { it.round == editingRound }

        viewModelScope.launch {
            val updatedScores = scores.map { (playerId, value) ->
                val existing = existingScores.find { it.playerId == playerId }
                RoundScore(
                    id = existing?.id ?: 0,
                    sessionId = sessionId,
                    playerId = playerId,
                    round = editingRound,
                    points = value.toInt(),
                )
            }
            updateRoundScoresUseCase(updatedScores)
            _uiState.update {
                it.copy(
                    showScoreEntry = false,
                    editingRound = null,
                    roundInputs = detail.players.associate { p -> p.id to "" },
                )
            }
        }
    }

    // ---- Delete round ----

    fun showDeleteRoundDialog(round: Int) =
        _uiState.update { it.copy(roundToDelete = round) }

    fun hideDeleteRoundDialog() =
        _uiState.update { it.copy(roundToDelete = null) }

    fun confirmDeleteRound() {
        val round = _uiState.value.roundToDelete ?: return
        viewModelScope.launch {
            deleteRoundUseCase(sessionId, round)
            _uiState.update { it.copy(roundToDelete = null) }
        }
    }

    // ---- Finish session ----

    fun showFinishDialog() = _uiState.update { it.copy(showFinishDialog = true) }
    fun hideFinishDialog() = _uiState.update { it.copy(showFinishDialog = false) }

    fun finishSession() {
        viewModelScope.launch {
            finishSessionUseCase(sessionId)
            _uiState.update { it.copy(showFinishDialog = false) }
        }
    }
}
