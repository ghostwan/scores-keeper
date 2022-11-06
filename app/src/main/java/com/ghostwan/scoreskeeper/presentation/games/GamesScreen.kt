@file:OptIn(ExperimentalMaterialApi::class)

package com.ghostwan.scoreskeeper.presentation.games

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
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
fun GamesScreen(viewModel: GamesViewModel = hiltViewModel(),
                navigateToUpdateGameScreen: (gameId: Int) -> Unit) {

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
                deleteGame = { viewModel.deleteGame(it) },
                navigateToUpdateGameScreen = navigateToUpdateGameScreen
            )
            AddBookDialog(
                isDialogOpened = viewModel.isDialogOpened,
                closeDialog = {
                    viewModel.closeDialog()
                },
                addGame = { game ->
                    viewModel.addGame(game)
                }
            )
        },
        floatingActionButton = {
            AddGameFloatingActionButton(
                openDialog = {
                    viewModel.openDialog()
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
        deleteGame = { Toast.makeText(context, "deleting the game", Toast.LENGTH_SHORT).show()},
        navigateToUpdateGameScreen = { Toast.makeText(context, "displaying update", Toast.LENGTH_SHORT).show()})
}

@Composable
fun GamesContent(padding: PaddingValues,
                 games: List<Game>,
                 deleteGame: (game: Game) -> Unit,
                 navigateToUpdateGameScreen: (bookId: Int) -> Unit) {

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
                deleteGame = { deleteGame(it) },
                navigateToUpdateGameScreen = navigateToUpdateGameScreen
            )
        }
    }

}

@Composable
fun AddGameFloatingActionButton(openDialog: () -> Unit) {
    FloatingActionButton(onClick = openDialog) {
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
fun GameCard(
    game: Game,
    deleteGame: () -> Unit,
    navigateToUpdateGameScreen: (bookId: Int) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .fillMaxWidth(),
        elevation = 3.dp,
        onClick = {
            navigateToUpdateGameScreen(game.id)
        }
    ) {
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
            DeleteIcon(
                deleteBook = deleteGame
            )
        }
    }
}

@Composable
fun DeleteIcon(
    deleteBook: () -> Unit
) {
    IconButton(
        onClick = deleteBook
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.delete_book),
        )
    }
}