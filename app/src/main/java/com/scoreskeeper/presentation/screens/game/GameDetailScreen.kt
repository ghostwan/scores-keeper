package com.scoreskeeper.presentation.screens.game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scoreskeeper.R
import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.model.PlayerStats
import com.scoreskeeper.domain.model.Session
import com.scoreskeeper.domain.model.SessionStatus
import com.scoreskeeper.presentation.components.PlayerAvatar
import com.scoreskeeper.presentation.screens.session.CreatePlayerViewModel
import com.scoreskeeper.presentation.theme.PlayerColors
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.newSessionId) {
        state.newSessionId?.let { id ->
            viewModel.onSessionNavigated()
            onNavigateToSession(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.game?.name ?: stringResource(R.string.game_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::showNewSessionSheet,
                icon = { Icon(Icons.Default.PlayArrow, null) },
                text = { Text(stringResource(R.string.new_session)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            // Stats section
            if (state.stats.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.general_ranking))
                }
                items(state.stats) { stats ->
                    StatsCard(stats = stats)
                }
            }

            // Sessions section
            if (state.sessions.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.sessions_header)) }
                items(state.sessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        onClick = { onNavigateToSession(session.id) },
                        onLongClick = { viewModel.showDeleteSessionDialog(session) },
                    )
                }
            }

            if (state.sessions.isEmpty() && state.stats.isEmpty() && !state.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.SportsEsports,
                            null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.no_sessions_played),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    // Delete session confirmation dialog
    if (state.sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteSessionDialog,
            title = { Text(stringResource(R.string.delete_session_title)) },
            text = { Text(stringResource(R.string.delete_session_message)) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDeleteSession,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteSessionDialog) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    // New session bottom sheet
    if (state.showNewSessionSheet) {
        NewSessionBottomSheet(
            allPlayers = state.allPlayers,
            selectedPlayers = state.selectedPlayers,
            minPlayers = state.game?.minPlayers ?: 2,
            maxPlayers = state.game?.maxPlayers ?: 10,
            onTogglePlayer = viewModel::togglePlayerSelection,
            onConfirm = viewModel::createSession,
            onDismiss = viewModel::hideNewSessionSheet,
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun StatsCard(stats: PlayerStats, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerAvatar(name = stats.player.name, color = stats.player.avatarColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(stats.player.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    stringResource(R.string.stats_subtitle, stats.gamesPlayed, stats.gamesWon),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${(stats.winRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.win_rate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionCard(session: Session, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                if (session.status == SessionStatus.IN_PROGRESS) Icons.Default.PlayArrow
                else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (session.status == SessionStatus.IN_PROGRESS)
                    MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.startedAt.format(formatter),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    stringResource(R.string.session_players_count, session.playerIds.size) +
                            if (session.status == SessionStatus.IN_PROGRESS) stringResource(R.string.status_in_progress)
                            else stringResource(R.string.status_finished),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewSessionBottomSheet(
    allPlayers: List<Player>,
    selectedPlayers: List<Player>,
    minPlayers: Int,
    maxPlayers: Int,
    onTogglePlayer: (Player) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    createPlayerViewModel: CreatePlayerViewModel = hiltViewModel(),
) {
    var showAddPlayerDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.choose_players),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.players_selection_subtitle, minPlayers, maxPlayers, selectedPlayers.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalIconButton(onClick = { showAddPlayerDialog = true }) {
                    Icon(Icons.Default.PersonAdd, stringResource(R.string.add_player))
                }
            }
            Spacer(Modifier.height(16.dp))

            if (allPlayers.isEmpty()) {
                Text(
                    stringResource(R.string.no_players_created),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                allPlayers.forEach { player ->
                    val isSelected = player in selectedPlayers
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTogglePlayer(player) },
                        label = { Text(player.name) },
                        leadingIcon = {
                            PlayerAvatar(name = player.name, color = player.avatarColor, size = 24.dp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onConfirm,
                enabled = selectedPlayers.size >= minPlayers,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.start_session))
            }
        }
    }

    if (showAddPlayerDialog) {
        AddPlayerDialog(
            onDismiss = { showAddPlayerDialog = false },
            onConfirm = { name, colorIdx ->
                createPlayerViewModel.createPlayer(name, colorIdx) {
                    showAddPlayerDialog = false
                }
            },
        )
    }
}

@Composable
private fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColorIdx by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_player)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.player_name_label)) },
                    singleLine = true,
                )
                Text(stringResource(R.string.color_label), style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlayerColors.take(5).forEachIndexed { idx, color ->
                        ColorDot(
                            color = color,
                            isSelected = selectedColorIdx == idx,
                            onClick = { selectedColorIdx = idx },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlayerColors.drop(5).forEachIndexed { i, color ->
                        val idx = i + 5
                        ColorDot(
                            color = color,
                            isSelected = selectedColorIdx == idx,
                            onClick = { selectedColorIdx = idx },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColorIdx) },
                enabled = name.isNotBlank(),
            ) { Text(stringResource(R.string.create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun ColorDot(
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color,
            modifier = Modifier.fillMaxSize(),
        ) {}
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
