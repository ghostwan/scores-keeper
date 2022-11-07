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

    var dialogToDisplay:GameDialog by mutableStateOf(GameDialog.NoDialog)

    val gamesFlow = repository.getGames()


//    fun getGame(id: Int) = viewModelScope.launch(Dispatchers.IO) {
//        game = repository.getGame(id)
//    }
//
//    fun updateName(name: String) {
//        game = game?.copy(name = name)
//    }
//
//    fun updateClassification(classification: GameClassification) {
//        game = game?.copy(classification = classification)
//    }

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

    fun openAddDialog() {
        dialogToDisplay = GameDialog.AddDialog
    }

    fun openEditDialog(game: Game) {
        dialogToDisplay = GameDialog.EditDialog(game)
    }

    fun openDeletionDialog(game: Game) {
        dialogToDisplay = GameDialog.DeletionDialog(game)
    }

    fun closeDialogs() {
        dialogToDisplay = GameDialog.NoDialog
    }
}

sealed class GameDialog {
    class EditDialog(val game: Game) : GameDialog()
    object AddDialog : GameDialog()
    class DeletionDialog(val game: Game) : GameDialog()
    object NoDialog : GameDialog()
}