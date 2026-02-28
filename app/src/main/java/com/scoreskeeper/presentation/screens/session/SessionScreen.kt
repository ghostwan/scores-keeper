package com.scoreskeeper.presentation.screens.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.model.SessionDetail
import com.scoreskeeper.domain.model.SessionStatus
import com.scoreskeeper.presentation.components.PlayerAvatar
import com.scoreskeeper.presentation.components.ScoreRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = state.detail
    val isInProgress = detail?.session?.status == SessionStatus.IN_PROGRESS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.session?.gameName ?: "Partie") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    if (isInProgress) {
                        TextButton(onClick = viewModel::showFinishDialog) {
                            Text("Terminer")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (isInProgress) {
                FloatingActionButton(onClick = viewModel::showScoreEntry) {
                    Icon(Icons.Default.Add, "Ajouter un tour")
                }
            }
        },
    ) { padding ->
        if (state.isLoading || detail == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            // Status banner if finished
            if (detail.session.status == SessionStatus.FINISHED && detail.winners != null) {
                item {
                    WinnerBanner(winners = detail.winners!!)
                }
            }

            // Scoreboard
            item {
                Text(
                    "Scores",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            items(detail.ranking.indices.toList()) { index ->
                val (player, total) = detail.ranking[index]
                val isWinner = detail.winners?.contains(player) == true
                ScoreRow(
                    rank = index + 1,
                    playerName = player.name,
                    playerColor = player.avatarColor,
                    totalScore = total,
                    isWinner = isWinner,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                )
            }

            // Score chart
            if (detail.rounds.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Évolution des scores",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    ScoreChart(detail = detail)
                }
            }

            // Round history
            if (detail.rounds.isNotEmpty()) {
                item {
                    Text(
                        "Historique des tours",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                val maxRound = detail.rounds.maxOf { it.round }
                items((1..maxRound).toList()) { round ->
                    RoundHistoryRow(
                        round = round,
                        players = detail.players,
                        rounds = detail.rounds.filter { it.round == round },
                        isEditable = isInProgress,
                        onEdit = { viewModel.editRound(round) },
                        onDelete = { viewModel.showDeleteRoundDialog(round) },
                    )
                }
            }
        }
    }

    // Score entry bottom sheet (for new round or editing)
    if (state.showScoreEntry) {
        val isEditing = state.editingRound != null
        ScoreEntryBottomSheet(
            players = detail?.players ?: emptyList(),
            roundNumber = state.editingRound ?: (detail?.currentRound ?: 1),
            inputs = state.roundInputs,
            isEditing = isEditing,
            onInput = viewModel::onScoreInput,
            onConfirm = if (isEditing) viewModel::submitEditRound else viewModel::submitRound,
            onDismiss = viewModel::hideScoreEntry,
        )
    }

    // Finish confirmation dialog
    if (state.showFinishDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideFinishDialog,
            title = { Text("Terminer la partie ?") },
            text = { Text("Le classement actuel sera enregistré définitivement.") },
            confirmButton = {
                Button(onClick = viewModel::finishSession) { Text("Terminer") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideFinishDialog) { Text("Annuler") }
            },
        )
    }

    // Delete round confirmation dialog
    if (state.roundToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteRoundDialog,
            title = { Text("Supprimer le tour ${state.roundToDelete} ?") },
            text = { Text("Les scores de ce tour seront supprimés définitivement.") },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDeleteRound,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteRoundDialog) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun WinnerBanner(winners: List<Player>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
            Column {
                Text(
                    if (winners.size == 1) "Victoire !" else "Egalité !",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    winners.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ScoreChart(detail: SessionDetail) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val maxRound = detail.rounds.maxOf { it.round }
    val playerColors = detail.players.map { Color(it.avatarColor) }

    LaunchedEffect(detail.rounds) {
        modelProducer.runTransaction {
            lineSeries {
                detail.players.forEach { player ->
                    val cumulativeScores = (1..maxRound).map { round ->
                        detail.rounds
                            .filter { it.playerId == player.id && it.round <= round }
                            .sumOf { it.points }
                            .toFloat()
                    }
                    series(cumulativeScores)
                }
            }
        }
    }

    val lineSpecs = playerColors.map { color ->
        rememberLine(fill = LineCartesianLayer.LineFill.single(fill(color)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(lineSpecs),
                    ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )
            // Légende
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                detail.players.forEach { player ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(player.avatarColor), shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoundHistoryRow(
    round: Int,
    players: List<Player>,
    rounds: List<com.scoreskeeper.domain.model.RoundScore>,
    isEditable: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .then(
                if (isEditable) {
                    Modifier.combinedClickable(
                        onClick = onEdit,
                        onLongClick = onDelete,
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Tour $round",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                if (isEditable) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                players.forEach { player ->
                    val score = rounds.find { it.playerId == player.id }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlayerAvatar(name = player.name, color = player.avatarColor, size = 28.dp)
                        Text(
                            text = score?.points?.toString() ?: "-",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = when {
                                score == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                score.points > 0 -> MaterialTheme.colorScheme.primary
                                score.points < 0 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoreEntryBottomSheet(
    players: List<Player>,
    roundNumber: Int,
    inputs: Map<Long, String>,
    isEditing: Boolean = false,
    onInput: (Long, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val allFilled = players.all { inputs[it.id]?.toIntOrNull() != null }
    val title = if (isEditing) {
        "Modifier le tour $roundNumber"
    } else {
        "Tour $roundNumber — Saisir les scores"
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))

            players.forEach { player ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PlayerAvatar(name = player.name, color = player.avatarColor, size = 36.dp)
                    Text(
                        player.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    OutlinedTextField(
                        value = inputs[player.id] ?: "",
                        onValueChange = { onInput(player.id, it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(100.dp),
                        placeholder = { Text("pts", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        isError = inputs[player.id]?.let {
                            it.isNotEmpty() && it != "-" && it.toIntOrNull() == null
                        } == true,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onConfirm,
                enabled = allFilled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isEditing) "Modifier le tour" else "Valider le tour")
            }
        }
    }
}
