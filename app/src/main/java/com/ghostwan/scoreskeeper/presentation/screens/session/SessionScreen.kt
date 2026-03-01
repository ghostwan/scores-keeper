package com.ghostwan.scoreskeeper.presentation.screens.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.domain.model.Player
import com.ghostwan.scoreskeeper.domain.model.SessionDetail
import com.ghostwan.scoreskeeper.domain.model.SessionStatus
import com.ghostwan.scoreskeeper.presentation.components.PlayerAvatar
import com.ghostwan.scoreskeeper.presentation.components.ScoreRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val chartAreaFill by viewModel.chartAreaFill.collectAsStateWithLifecycle()
    val chartStartFromZero by viewModel.chartStartFromZero.collectAsStateWithLifecycle()
    val detail = state.detail
    val isInProgress = detail?.session?.status == SessionStatus.IN_PROGRESS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.session?.gameName ?: stringResource(R.string.session_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    if (isInProgress) {
                        TextButton(onClick = viewModel::showFinishDialog) {
                            Text(stringResource(R.string.finish))
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (isInProgress) {
                FloatingActionButton(onClick = viewModel::showScoreEntry) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_round))
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
                    stringResource(R.string.scores_header),
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
                    onClick = { viewModel.showEditPlayerDialog(player) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                )
            }

            // Score chart
            if (detail.rounds.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.score_evolution),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    ScoreChart(detail = detail, areaFill = chartAreaFill, startFromZero = chartStartFromZero)
                }
            }

            // Round history
            if (detail.rounds.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.rounds_history),
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
            title = { Text(stringResource(R.string.finish_session_title)) },
            text = { Text(stringResource(R.string.finish_session_message)) },
            confirmButton = {
                Button(onClick = viewModel::finishSession) { Text(stringResource(R.string.finish)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideFinishDialog) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    // Delete round confirmation dialog
    if (state.roundToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteRoundDialog,
            title = { Text(stringResource(R.string.delete_round_title, state.roundToDelete!!)) },
            text = { Text(stringResource(R.string.delete_round_message)) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDeleteRound,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteRoundDialog) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    // Edit player name dialog
    if (state.editingPlayer != null) {
        AlertDialog(
            onDismissRequest = viewModel::hideEditPlayerDialog,
            title = { Text(stringResource(R.string.edit_player_name_title)) },
            text = {
                OutlinedTextField(
                    value = state.editPlayerName,
                    onValueChange = viewModel::onEditPlayerNameChange,
                    label = { Text(stringResource(R.string.player_name_label)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmEditPlayer,
                    enabled = state.editPlayerName.isNotBlank(),
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideEditPlayerDialog) { Text(stringResource(R.string.cancel)) }
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
                    if (winners.size == 1) stringResource(R.string.victory) else stringResource(R.string.tie),
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
private fun ScoreChart(detail: SessionDetail, areaFill: Boolean = true, startFromZero: Boolean = false) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val maxRound = detail.rounds.maxOf { it.round }
    val playerColors = detail.players.map { Color(it.avatarColor) }
    var selectedPlayerIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(detail.rounds, startFromZero) {
        modelProducer.runTransaction {
            lineSeries {
                detail.players.forEach { player ->
                    val cumulativeScores = (1..maxRound).map { round ->
                        detail.rounds
                            .filter { it.playerId == player.id && it.round <= round }
                            .sumOf { it.points }
                            .toFloat()
                    }
                    val scores = if (startFromZero) listOf(0f) + cumulativeScores else cumulativeScores
                    series(scores)
                }
            }
        }
    }

    val lineSpecs = playerColors.mapIndexed { index, color ->
        val isSelected = selectedPlayerIndex == index
        val isAnySelected = selectedPlayerIndex >= 0
        val displayColor = when {
            !isAnySelected -> color
            isSelected -> color
            else -> color.copy(alpha = 0.15f)
        }
        val thickness = if (isSelected) 4.dp else 2.dp
        val lineFill = LineCartesianLayer.LineFill.single(fill(displayColor))
        val lineAreaFill = if (areaFill) {
            LineCartesianLayer.AreaFill.single(fill(displayColor.copy(alpha = 0.2f)))
        } else {
            null
        }
        rememberLine(
            fill = lineFill,
            areaFill = lineAreaFill,
            thickness = thickness,
        )
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
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                detail.players.forEachIndexed { index, player ->
                    val isSelected = selectedPlayerIndex == index
                    val isAnySelected = selectedPlayerIndex >= 0
                    Surface(
                        onClick = {
                            selectedPlayerIndex = if (isSelected) -1 else index
                        },
                        shape = MaterialTheme.shapes.small,
                        color = if (isSelected) {
                            Color(player.avatarColor).copy(alpha = 0.15f)
                        } else {
                            Color.Transparent
                        },
                        modifier = Modifier.padding(vertical = 2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = if (!isAnySelected || isSelected) {
                                            Color(player.avatarColor)
                                        } else {
                                            Color(player.avatarColor).copy(alpha = 0.3f)
                                        },
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                    )
                            )
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isAnySelected || isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                            )
                        }
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
    rounds: List<com.ghostwan.scoreskeeper.domain.model.RoundScore>,
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
                    stringResource(R.string.round_number, round),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                if (isEditable) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
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
        stringResource(R.string.edit_round_title, roundNumber)
    } else {
        stringResource(R.string.new_round_title, roundNumber)
    }

    val focusRequesters = remember(players) { players.map { FocusRequester() } }

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

            players.forEachIndexed { index, player ->
                val isLast = index == players.lastIndex
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (!isLast) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            },
                            onDone = {
                                if (allFilled) onConfirm()
                            },
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .width(100.dp)
                            .focusRequester(focusRequesters[index]),
                        placeholder = { Text(stringResource(R.string.pts), color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                Text(if (isEditing) stringResource(R.string.edit_round_button) else stringResource(R.string.validate_round_button))
            }
        }
    }

    // Auto-focus the first field when the sheet opens
    LaunchedEffect(Unit) {
        if (focusRequesters.isNotEmpty()) {
            focusRequesters[0].requestFocus()
        }
    }
}
