package com.scoreskeeper.presentation.screens.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreskeeper.domain.model.RoundScore
import com.scoreskeeper.domain.model.SessionDetail
import com.scoreskeeper.domain.usecase.session.AddRoundScoreUseCase
import com.scoreskeeper.domain.usecase.session.FinishSessionUseCase
import com.scoreskeeper.domain.usecase.session.GetSessionDetailUseCase
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
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSessionDetailUseCase: GetSessionDetailUseCase,
    private val addRoundScoreUseCase: AddRoundScoreUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

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
            roundInputs = state.detail?.players?.associate { it.id to "" } ?: emptyMap(),
        )
    }

    fun hideScoreEntry() = _uiState.update { it.copy(showScoreEntry = false) }

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

    fun showFinishDialog() = _uiState.update { it.copy(showFinishDialog = true) }
    fun hideFinishDialog() = _uiState.update { it.copy(showFinishDialog = false) }

    fun finishSession() {
        viewModelScope.launch {
            finishSessionUseCase(sessionId)
            _uiState.update { it.copy(showFinishDialog = false) }
        }
    }
}
