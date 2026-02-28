package com.scoreskeeper.presentation.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scoreskeeper.domain.model.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateGame: () -> Unit,
    onNavigateToGame: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    var gameToDelete by remember { mutableStateOf<Game?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scores Keeper") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateGame,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouveau jeu") },
            )
        },
    ) { padding ->
        if (games.isEmpty()) {
            EmptyGamesPlaceholder(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onCreateGame = onNavigateToCreateGame,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(games, key = { it.id }) { game ->
                    GameCard(
                        game = game,
                        onClick = { onNavigateToGame(game.id) },
                        onLongClick = { gameToDelete = game },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    gameToDelete?.let { game ->
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            title = { Text("Supprimer ${game.name} ?") },
            text = { Text("Toutes les parties associées seront également supprimées.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGame(game)
                        gameToDelete = null
                    }
                ) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) { Text("Annuler") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameCard(
    game: Game,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (game.description.isNotBlank()) {
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "${game.minPlayers}-${game.maxPlayers} joueurs" +
                            if (game.lowestScoreWins) " · Score le plus bas gagne" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyGamesPlaceholder(
    onCreateGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Aucun jeu créé",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FilledTonalButton(onClick = onCreateGame) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Créer mon premier jeu")
        }
    }
}
