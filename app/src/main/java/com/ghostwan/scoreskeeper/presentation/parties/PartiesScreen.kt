package com.ghostwan.scoreskeeper.presentation.parties

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.model.Party
import com.ghostwan.scoreskeeper.presentation.ui.DeletionDialog
import java.util.*

@Composable
fun PartiesScreen(
    gameId: Long,
    viewModel: PartiesViewModel = hiltViewModel(),
    navigateBack: () -> Boolean
) {

    LaunchedEffect(Unit) {
        viewModel.getGame(gameId)
    }

    Scaffold(
        topBar = {
            PartiesTopBar()
        },
        content = { padding ->
            PartiesContent(
                padding = padding,
                parties = viewModel.game.parties,
            )
            when (viewModel.dialogToDisplay) {
                PartyDialog.AddDialog -> {
                    AddPartyDialog(
                        viewModel.game,
                        onStart = { party -> viewModel.newParty(party) },
                        onDialogClosing = { viewModel.closeDialogs() }
                    )
                }
                is PartyDialog.DeletionDialog -> {
                    DeletionDialog(
                        entity = (viewModel.dialogToDisplay as PartyDialog.DeletionDialog).party,
                        onClose = { viewModel.closeDialogs() },
                        onDelete = { party -> viewModel.deleteParty(party)})
                }
                else -> {/* Hide the dialog*/ }
            }
        },
        floatingActionButton = {
            AddFloatingActionButton(
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
    PartiesContent(
        padding = PaddingValues(2.dp),
        parties = listOf(
            Party(0, Date(), mutableListOf("Erwan", "Kai")),
            Party(1, Date(), mutableListOf("Erwan", "Kai", "Arnold")),
            Party(2, Date(), mutableListOf("Erwan", "Kai", "Arnold", "Camille")),
        )
    )
}

@Composable
fun PartiesContent(padding: PaddingValues,
                   parties: List<Party>) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        items(
            items = parties
        ) {
            PartyCard(it)
        }
    }

}

@Composable
fun AddFloatingActionButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.new_game))
    }
}

@Composable
fun PartiesTopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.parties_title)) },
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
fun LongPressMenu(
    isMenuDisplayed: Boolean,
    party: Party,
    viewModel: PartiesViewModel = hiltViewModel(),
    onMenuClosing: ()-> Unit,
) {

    DropdownMenu(
        expanded = isMenuDisplayed,
        onDismissRequest = onMenuClosing
    ) {
        DropdownMenuItem(
            onClick = {
                viewModel.deleteParty(party)
                onMenuClosing()
            }
        ) {
            Text(text = "Delete")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PartyCard(party: Party) {
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
        LongPressMenu(isMenuDisplayed, party) {
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
                    text = party.date.toString(),
                    color = Color.DarkGray,
                    fontSize = 25.sp
                )
            }
            Spacer(
                modifier = Modifier.weight(1f)
            )
//            Text(
//                text = "${party.}",
//                color = MaterialTheme.colors.primary,
//                fontSize = 16.sp,
//            )
        }
    }
}