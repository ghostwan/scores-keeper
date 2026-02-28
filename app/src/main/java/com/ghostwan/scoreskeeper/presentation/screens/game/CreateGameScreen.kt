package com.ghostwan.scoreskeeper.presentation.screens.game

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostwan.scoreskeeper.R

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
                title = { Text(stringResource(R.string.create_game_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
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
                label = { Text(stringResource(R.string.game_name_label)) },
                placeholder = { Text(stringResource(R.string.game_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = state.name.isBlank(),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.description_label)) },
                placeholder = { Text(stringResource(R.string.description_placeholder)) },
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
                        stringResource(R.string.min_players),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    PlayerCountPicker(
                        value = state.minPlayers,
                        min = 2,
                        max = if (state.maxPlayers == Int.MAX_VALUE) 99 else state.maxPlayers,
                        onValueChange = viewModel::onMinPlayersChange,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.max_players),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    MaxPlayerCountPicker(
                        value = state.maxPlayers,
                        min = state.minPlayers,
                        onValueChange = viewModel::onMaxPlayersChange,
                        onUnlimited = viewModel::setMaxPlayersUnlimited,
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
                            stringResource(R.string.lowest_score_wins),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.lowest_score_wins_description),
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

@Composable
private fun MaxPlayerCountPicker(
    value: Int,
    min: Int,
    onValueChange: (Int) -> Unit,
    onUnlimited: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUnlimited = value == Int.MAX_VALUE
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilledIconButton(
            onClick = {
                if (isUnlimited) {
                    onValueChange(min)
                } else {
                    onValueChange(value - 1)
                }
            },
            enabled = !isUnlimited && value > min,
            modifier = Modifier.size(36.dp),
        ) {
            Text("-", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = if (isUnlimited) "\u221E" else value.toString(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        FilledIconButton(
            onClick = { onValueChange(value + 1) },
            enabled = !isUnlimited,
            modifier = Modifier.size(36.dp),
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
        FilledTonalIconButton(
            onClick = {
                if (isUnlimited) onValueChange(min) else onUnlimited()
            },
            modifier = Modifier.size(36.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (isUnlimited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isUnlimited) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text("\u221E", style = MaterialTheme.typography.titleMedium)
        }
    }
}
