package com.scoreskeeper.presentation.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Google Drive section
            Text(
                "Sauvegarde Google Drive",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (syncState.accountEmail != null) {
                        // Connected state
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    syncState.accountName ?: syncState.accountEmail!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    syncState.accountEmail!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Sync status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (syncState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Text(
                                    "Synchronisation en cours...",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            } else if (syncState.lastSyncTime > 0) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
                                Text(
                                    "Dernière sync : ${dateFormat.format(Date(syncState.lastSyncTime))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (syncState.lastError != null) {
                            Text(
                                "Erreur : ${syncState.lastError}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        Text(
                            "La synchronisation est automatique après chaque modification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        HorizontalDivider()

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.manualBackup() },
                                enabled = !syncState.isSyncing,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sauvegarder")
                            }
                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                enabled = !syncState.isSyncing,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Restaurer")
                            }
                        }

                        TextButton(
                            onClick = { viewModel.disconnect() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                        ) {
                            Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Déconnecter")
                        }
                    } else {
                        // Not connected state
                        Text(
                            "Connectez votre compte Google pour sauvegarder automatiquement vos données sur Google Drive.",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        GoogleSignInButton(
                            onSignedIn = { email, name ->
                                viewModel.onSignedIn(email, name)
                            },
                        )
                    }
                }
            }

            // App info
            Text(
                "À propos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Scores Keeper", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Restore confirmation dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restaurer les données ?") },
            text = {
                Text("Les données actuelles seront remplacées par la sauvegarde de Google Drive. L'application devra être redémarrée.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    viewModel.restoreBackup()
                }) { Text("Restaurer") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun GoogleSignInButton(
    onSignedIn: (email: String, displayName: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {
                try {
                    val credentialManager = CredentialManager.create(context)

                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential

                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        onSignedIn(
                            googleCredential.id,
                            googleCredential.displayName ?: googleCredential.id,
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("GoogleSignIn", "Sign-in failed", e)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Connecter Google Drive")
    }
}
