package com.ghostwan.scoreskeeper.presentation.parties

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.model.Game
import com.ghostwan.scoreskeeper.model.Party
import java.util.*


/**
 * Dialog that allow to add or edit a game
 *
 * @param onDialogClosing callback to call when dialog is closing
 * @param onAddGame callback to call when a game is added
 * @param onEditGame callback to call when a editing a game
 * @param game  game to edit, let null if you want to add a new game
 */
@Composable
fun AddPartyDialog(
    game: Game,
    onDialogClosing: () -> Unit,
    onStart: (party: Party) -> Unit,
) {
    val focusRequester = FocusRequester()
    val party by remember { mutableStateOf(Party(0, Date(), arrayListOf()))}
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDialogClosing,
        title = { Text( "New party for ${game.name}") },
        text = {
            Column {
                TextField(
                    value = name, onValueChange = { name = it },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    placeholder = { Text(stringResource(R.string.name)) },
                    keyboardActions = KeyboardActions(onDone = {
                        party.players.add(name)
                        name = ""
                    }),
                    modifier = Modifier.focusRequester(focusRequester)
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(
                        items = party.players
                    ) {
                        Text(text = it)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDialogClosing()
                    onStart(party)
                }
            ) {
                Text("Start")
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