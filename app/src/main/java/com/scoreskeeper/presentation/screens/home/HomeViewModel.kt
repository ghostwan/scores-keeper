package com.scoreskeeper.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreskeeper.domain.model.Game
import com.scoreskeeper.domain.usecase.game.CreateGameUseCase
import com.scoreskeeper.domain.usecase.game.DeleteGameUseCase
import com.scoreskeeper.domain.usecase.game.GetAllGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllGamesUseCase: GetAllGamesUseCase,
    private val createGameUseCase: CreateGameUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
) : ViewModel() {

    val games: StateFlow<List<Game>> = getAllGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteGame(game: Game) {
        viewModelScope.launch { deleteGameUseCase(game) }
    }
}
