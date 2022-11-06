package com.ghostwan.scoreskeeper.presentation.games

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostwan.scoreskeeper.db.GameRepository
import com.ghostwan.scoreskeeper.model.Game
import com.ghostwan.scoreskeeper.model.GameClassification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    var game by mutableStateOf(Game(0, "", GameClassification.highest))
    var isDialogOpened by mutableStateOf(false)

    val gamesFlow = repository.getGames()


    fun getGame(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        game = repository.getGame(id)
    }

    fun updateName(name: String) {
        game = game.copy(name = name)
    }

    fun updateClassification(classification: GameClassification) {
        game = game.copy(classification = classification)
    }

    fun addGame(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGame(game)
        }
    }

    fun updateGame(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGame(game)
        }
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGame(game)
        }
    }

    fun openDialog() {
        isDialogOpened = true
    }

    fun closeDialog() {
        isDialogOpened = false
    }
}