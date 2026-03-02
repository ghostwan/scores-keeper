package com.ghostwan.scoreskeeper.presentation.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostwan.scoreskeeper.domain.model.Game
import com.ghostwan.scoreskeeper.domain.usecase.game.DeleteGameUseCase
import com.ghostwan.scoreskeeper.domain.usecase.game.GetAllGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllGamesUseCase: GetAllGamesUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
) : ViewModel() {

    val games: StateFlow<List<Game>> = getAllGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            try {
                deleteGameUseCase(game)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete game", e)
                _error.value = e.message
            }
        }
    }

    fun clearError() { _error.value = null }

    companion object {
        private const val TAG = "HomeVM"
    }
}
