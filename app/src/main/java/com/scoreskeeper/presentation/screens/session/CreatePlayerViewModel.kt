package com.scoreskeeper.presentation.screens.session

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.usecase.player.CreatePlayerUseCase
import com.scoreskeeper.presentation.theme.PlayerColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePlayerViewModel @Inject constructor(
    private val createPlayerUseCase: CreatePlayerUseCase,
) : ViewModel() {

    fun createPlayer(name: String, colorIndex: Int, onCreated: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            createPlayerUseCase(
                Player(
                    name = name.trim(),
                    avatarColor = PlayerColors[colorIndex.coerceIn(0, PlayerColors.lastIndex)].toArgb().toLong() and 0xFFFFFFFFL,
                )
            )
            onCreated()
        }
    }
}
