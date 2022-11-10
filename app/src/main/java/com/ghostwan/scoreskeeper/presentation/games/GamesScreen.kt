package com.ghostwan.scoreskeeper.presentation.games

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.model.Game
import com.ghostwan.scoreskeeper.model.GameClassification

@Composable
fun GamesScreen(viewModel: GamesViewModel = hiltViewModel()) {

    val games by viewModel.gamesFlow.collectAsState(
        emptyList()
    )

    Scaffold(
        topBar = {
            GamesTopBar()
        },
        content = { padding ->
            GamesContent(
                padding = padding,
                games = games,
                onDeleteGame = { game -> viewModel.openDeletionDialog(game) },
                onEditGame = { game -> viewModel.openEditDialog(game) }
            )
            when (viewModel.dialogToDisplay) {
                GameDialog.AddDialog -> {
                    AddGameDialog(
                        onAddGame = { game -> viewModel.addGame(game) },
                        onDialogClosing = { viewModel.closeDialogs() }
                    )
                }
                is GameDialog.EditDialog -> {
                    EditGameDialog(
                        game = (viewModel.dialogToDisplay as GameDialog.EditDialog).game,
                        onEditGame = { game -> viewModel.updateGame(game) },
                        onDialogClosing = { viewModel.closeDialogs()},
                    )
                }
                is GameDialog.DeletionDialog -> {
                    DeletionDialog(
                        game = (viewModel.dialogToDisplay as GameDialog.DeletionDialog).game,
                        onDialogClosing = { viewModel.closeDialogs() },
                        onDeletingGame = { game -> viewModel.deleteGame(game)})
                }
                else -> {/* Hide the dialog*/ }
            }
        },
        floatingActionButton = {
            AddGameFloatingActionButton(
                onClick = {
                    viewModel.openAddDialog()
                }
            )
        }
    )
}



@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFF)
fun Preview() {
    val context = LocalContext.current
    GamesContent(
        padding = PaddingValues(2.dp),
        games = listOf(
            Game(0, "Yaniv", GameClassification.lowest),
            Game(1, "Escalier", GameClassification.highest),
            Game(2, "Ratatouille", GameClassification.lowest),
        ),
        onDeleteGame = { Toast.makeText(context, "deleting the game", Toast.LENGTH_SHORT).show()},
        onEditGame = { Toast.makeText(context, "displaying update", Toast.LENGTH_SHORT).show()})
}

@Composable
fun GamesContent(padding: PaddingValues,
                 games: List<Game>,
                 onDeleteGame: (game: Game) -> Unit,
                 onEditGame: (game: Game) -> Unit) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        items(
            items = games
        ) {
            GameCard(
                game = it,
                onDeleteGame = { onDeleteGame(it) },
                onEditGame = { onEditGame(it) }
            )
        }
    }

}

@Composable
fun AddGameFloatingActionButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.new_game))
    }
}

@Composable
fun GamesTopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.games)) },
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
fun LongPressMenu(
    isMenuDisplayed: Boolean,
    onEditGame: () -> Unit,
    onDeleteGame: () -> Unit,
    onMenuClosing: ()-> Unit) {

    DropdownMenu(
        expanded = isMenuDisplayed,
        onDismissRequest = onMenuClosing
    ) {
        DropdownMenuItem(
            onClick = {
                onEditGame()
                onMenuClosing()
            }
        ) {
            Text(text = "Edit")
        }
        DropdownMenuItem(
            onClick = {
                onDeleteGame()
                onMenuClosing()
            }
        ) {
            Text(text = "Delete")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCard(
    game: Game,
    onDeleteGame: () -> Unit,
    onEditGame: () -> Unit,
) {
    var isMenuDisplayed by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .fillMaxWidth()
            .combinedClickable(onClick = {

            },
                onLongClick = {
                    isMenuDisplayed = true
                }),
        elevation = 3.dp
    ) {
        LongPressMenu(isMenuDisplayed, onEditGame, onDeleteGame) {
            isMenuDisplayed = false
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = game.name,
                    color = Color.DarkGray,
                    fontSize = 25.sp
                )
                Text(
                    text = game.classification.name,
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.Underline
                )
            }
            Spacer(
                modifier = Modifier.weight(1f)
            )
            Text(
//                text = "${game.parties.size}",
                text = "0",
                color = MaterialTheme.colors.primary,
                fontSize = 16.sp,
            )
        }
    }
}