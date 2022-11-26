package com.ghostwan.scoreskeeper.presentation.parties

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostwan.scoreskeeper.database.GameRepository
import com.ghostwan.scoreskeeper.model.Game
import com.ghostwan.scoreskeeper.model.Party
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PartiesViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    var dialogToDisplay:PartyDialog by mutableStateOf(PartyDialog.NoDialog)
    var game: Game = Game(0, "")
    var party: Party = Party(0, Date(), arrayListOf())

    fun getGame(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        game = repository.getGame(id)
    }

    fun newParty(party: Party) {
        viewModelScope.launch(Dispatchers.IO) {
            game.parties.add(party)
            repository.updateGame(game)
        }
    }

    fun deleteParty(party: Party) {
        viewModelScope.launch(Dispatchers.IO) {
            game.parties.remove(party)
            repository.updateGame(game)
        }
    }

    fun openAddDialog() {
        dialogToDisplay = PartyDialog.AddDialog
    }

    fun openDeletionDialog(party: Party) {
        dialogToDisplay = PartyDialog.DeletionDialog(party)
    }

    fun closeDialogs() {
        dialogToDisplay = PartyDialog.NoDialog
    }
}

sealed class PartyDialog {
    object AddDialog : PartyDialog()
    class DeletionDialog(val party: Party) : PartyDialog()
    object NoDialog : PartyDialog()
}