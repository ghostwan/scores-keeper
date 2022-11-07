package com.ghostwan.scoreskeeper.presentation.games

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.model.Game


//@Composable
//fun UpdateGameScreen(
//    viewModel: GamesViewModel = hiltViewModel(),
//    gameId: Int,
//    navigateBack: () -> Unit
//) {
//    LaunchedEffect(Unit) {
//        viewModel.getGame(gameId)
//    }
//    Scaffold(
//        topBar = {
//            UpdateGameTopBar(
//                navigateBack = navigateBack
//            )
//        },
//        content = { padding ->
//            UpdateGameContent(
//                padding = padding,
//                game = viewModel.game,
//                updateName = { name ->
//                    viewModel.updateName(name)
//                },
//                updateGame = { game ->
//                    viewModel.updateGame(game)
//                },
//                navigateBack = navigateBack
//            )
//        }
//    )
//}
//
//@Composable
//fun UpdateGameTopBar(
//    navigateBack: () -> Unit
//) {
//    TopAppBar (
//        title = {
//            Text(text = stringResource(R.string.update_game))
//        },
//        navigationIcon = {
//            IconButton(
//                onClick = navigateBack
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.ArrowBack,
//                    contentDescription = null,
//                )
//            }
//        }
//    )
//}
//
//@Composable
//fun UpdateGameContent(
//    padding: PaddingValues,
//    game: Game,
//    updateName: (title: String) -> Unit,
//    updateGame: (game: Game) -> Unit,
//    navigateBack: () -> Unit
//) {
//    Column(
//        modifier = Modifier.fillMaxSize().padding(padding),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        TextField(
//            value = game.name,
//            onValueChange = { name ->
//                updateName(name)
//            },
//            placeholder = {
//                Text(text = stringResource(R.string.name))
//            }
//        )
//        Spacer(
//            modifier = Modifier.height(8.dp)
//        )
//        Button(
//            onClick = {
//                updateGame(game)
//                navigateBack()
//            }
//        ) {
//            Text(text = stringResource(R.string.update))
//        }
//    }
//}