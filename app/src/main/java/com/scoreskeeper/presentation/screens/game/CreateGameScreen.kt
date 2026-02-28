package com.scoreskeeper.presentation.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGameScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateGameViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un jeu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::saveGame,
                containerColor = if (state.name.isBlank())
                    MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Check, contentDescription = "Enregistrer")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nom du jeu *") },
                placeholder = { Text("ex. Uno, Tarot, 7 Wonders...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = state.name.isBlank(),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optionnel)") },
                placeholder = { Text("Règles spéciales, notes...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            // Min/Max players
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Joueurs min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    PlayerCountPicker(
                        value = state.minPlayers,
                        min = 2,
                        max = state.maxPlayers,
                        onValueChange = viewModel::onMinPlayersChange,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Joueurs max",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    PlayerCountPicker(
                        value = state.maxPlayers,
                        min = state.minPlayers,
                        max = 20,
                        onValueChange = viewModel::onMaxPlayersChange,
                    )
                }
            }

            // Lowest score wins toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Score le plus bas gagne",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            "Pour les jeux de cartes ou le but est d'avoir le moins de points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.lowestScoreWins,
                        onCheckedChange = viewModel::onLowestScoreWinsChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerCountPicker(
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilledIconButton(
            onClick = { onValueChange(value - 1) },
            enabled = value > min,
            modifier = Modifier.size(36.dp),
        ) {
            Text("-", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        FilledIconButton(
            onClick = { onValueChange(value + 1) },
            enabled = value < max,
            modifier = Modifier.size(36.dp),
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
    }
}
