package com.ghostwan.scoreskeeper.data.backup

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import com.ghostwan.scoreskeeper.data.local.ScoresKeeperDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: ScoresKeeperDatabase,
) {
    companion object {
        private const val TAG = "DriveBackupService"
        private const val BACKUP_FILE_NAME = "scores_keeper_backup.db"
        private const val DB_NAME = "scores_keeper.db"
    }

    private fun getDriveService(accountEmail: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccountName = accountEmail

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ScoresKeeper")
            .build()
    }

    /**
     * Upload the Room database to Google Drive appDataFolder.
     * Returns true if successful.
     */
    suspend fun uploadBackup(accountEmail: String): BackupResult = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(accountEmail)
            val dbFile = context.getDatabasePath(DB_NAME)

            if (!dbFile.exists()) {
                return@withContext BackupResult.Error("Base de données introuvable")
            }

            // Close WAL checkpoint to ensure all data is in the main db file
            database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")

            // Check if backup already exists
            val existingFileId = findBackupFile(driveService)

            val mediaContent = FileContent("application/x-sqlite3", dbFile)

            if (existingFileId != null) {
                // Update existing file
                driveService.files().update(existingFileId, null, mediaContent).execute()
                Log.d(TAG, "Backup updated on Drive")
            } else {
                // Create new file
                val fileMetadata = DriveFile().apply {
                    name = BACKUP_FILE_NAME
                    parents = listOf("appDataFolder")
                }
                driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                Log.d(TAG, "Backup created on Drive")
            }

            BackupResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            BackupResult.Error(e.message ?: "Erreur inconnue")
        }
    }

    /**
     * Download and restore the Room database from Google Drive.
     * Returns true if successful.
     */
    suspend fun restoreBackup(accountEmail: String): BackupResult = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(accountEmail)
            val fileId = findBackupFile(driveService)
                ?: return@withContext BackupResult.Error("Aucune sauvegarde trouvée sur Drive")

            val dbFile = context.getDatabasePath(DB_NAME)
            val tempFile = java.io.File(dbFile.parent, "restore_temp.db")

            // Download to temp file
            FileOutputStream(tempFile).use { outputStream ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }

            // Replace main database
            dbFile.delete()
            // Also delete WAL and SHM files
            java.io.File(dbFile.path + "-wal").delete()
            java.io.File(dbFile.path + "-shm").delete()

            tempFile.renameTo(dbFile)

            Log.d(TAG, "Backup restored from Drive")
            BackupResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            BackupResult.Error(e.message ?: "Erreur inconnue")
        }
    }

    /**
     * Check if a backup exists on Drive.
     */
    suspend fun hasBackup(accountEmail: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(accountEmail)
            findBackupFile(driveService) != null
        } catch (e: Exception) {
            Log.e(TAG, "Check backup failed", e)
            false
        }
    }

    private fun findBackupFile(driveService: Drive): String? {
        val result = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE_NAME'")
            .setFields("files(id, name, modifiedTime)")
            .setPageSize(1)
            .execute()

        return result.files?.firstOrNull()?.id
    }
}

sealed class BackupResult {
    data object Success : BackupResult()
    data class Error(val message: String) : BackupResult()
}
