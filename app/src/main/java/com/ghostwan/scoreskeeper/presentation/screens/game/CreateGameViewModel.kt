package com.ghostwan.scoreskeeper.presentation.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostwan.scoreskeeper.domain.model.Game
import com.ghostwan.scoreskeeper.domain.usecase.game.CreateGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateGameUiState(
    val name: String = "",
    val description: String = "",
    val minPlayers: Int = 2,
    val maxPlayers: Int = Int.MAX_VALUE,
    val lowestScoreWins: Boolean = false,
    val isLoading: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class CreateGameViewModel @Inject constructor(
    private val createGameUseCase: CreateGameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGameUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onMinPlayersChange(value: Int) = _uiState.update {
        val max = if (it.maxPlayers == Int.MAX_VALUE) 99 else it.maxPlayers
        it.copy(minPlayers = value.coerceIn(2, max))
    }
    fun onMaxPlayersChange(value: Int) = _uiState.update {
        it.copy(maxPlayers = value.coerceAtLeast(it.minPlayers))
    }
    fun setMaxPlayersUnlimited() = _uiState.update {
        it.copy(maxPlayers = Int.MAX_VALUE)
    }
    fun onLowestScoreWinsChange(value: Boolean) = _uiState.update { it.copy(lowestScoreWins = value) }

    fun saveGame() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            createGameUseCase(
                Game(
                    name = state.name.trim(),
                    description = state.description.trim(),
                    minPlayers = state.minPlayers,
                    maxPlayers = state.maxPlayers,
                    lowestScoreWins = state.lowestScoreWins,
                )
            )
            _uiState.update { it.copy(isLoading = false, saved = true) }
        }
    }
}
