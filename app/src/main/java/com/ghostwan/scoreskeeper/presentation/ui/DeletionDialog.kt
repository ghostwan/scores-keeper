package com.ghostwan.scoreskeeper.presentation.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun <T> DeletionDialog(entity: T,
                       onClose: () -> Unit,
                       onDelete: (entity: T) -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Are you sure you want to delete $entity ?") },
        confirmButton = {
            TextButton(onClick = {
                onDelete(entity)
                onClose()
            }) {
                Text(
                    text = "confirm"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(
                    text = "dismiss"
                )
            }
        }
    )
}