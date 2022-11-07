package com.ghostwan.scoreskeeper.presentation.games

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.model.Game


@Composable
fun AddGameDialog(
    onAddGame: (game: Game) -> Unit,
    onDialogClosing: () -> Unit
) {
    AddOrEditGameDialog(
        onDialogClosing = onDialogClosing,
        onSubmit = { game -> onAddGame(game) }
    )
}

@Composable
fun EditGameDialog(
    game: Game,
    onEditGame: (game: Game) -> Unit,
    onDialogClosing: () -> Unit
) {
    AddOrEditGameDialog(
        onDialogClosing = onDialogClosing,
        onSubmit = { game2 -> onEditGame(game2) },
        game = game
    )
}

/**
 * Dialog that allow to add or edit a game
 *
 * @param onDialogClosing callback to call when dialog is closing
 * @param onAddGame callback to call when a game is added
 * @param onEditGame callback to call when a editing a game
 * @param game  game to edit, let null if you want to add a new game
 */
@Composable
private fun AddOrEditGameDialog(
    onDialogClosing: () -> Unit,
    onSubmit: (game: Game) -> Unit,
    game: Game? = null
) {
    val editing = game != null

    val id by remember { mutableStateOf(game?.id ?: 0) }
    var name by remember { mutableStateOf(game?.name ?: "") }
    val focusRequester = FocusRequester()

    AlertDialog(
        onDismissRequest = onDialogClosing,
        title = { Text(if (editing) "Edit ${game?.name}" else "Add new game") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.focusRequester(focusRequester)
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDialogClosing()
                    onSubmit(Game(id, name))
                }
            ) {
                Text(if (editing) "Edit" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDialogClosing) {
                Text(
                    text = "dismiss"
                )
            }
        }
    )
}

@Composable
fun DeletionDialog(game: Game,
                   onDialogClosing: () -> Unit,
                   onDeletingGame: (game: Game) -> Unit) {
    AlertDialog(
        onDismissRequest = onDialogClosing,
        title = { Text("Are you sure you want to delete ${game.name} ?") },
        confirmButton = {
            TextButton( onClick = {
                onDeletingGame(game)
                onDialogClosing()
            }) {
                Text(
                    text = "confirm"
                )
            }
        },
        dismissButton = {
            TextButton( onClick = onDialogClosing) {
                Text(
                    text = "dismiss"
                )
            }
        }
    )
}