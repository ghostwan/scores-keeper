package com.scoreskeeper.data.backup

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(activityContext: Context): GoogleSignInResult {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id,
                )
            } else {
                GoogleSignInResult.Error("Type de credential non supporté")
            }
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.message ?: "Erreur de connexion")
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
    }
}

sealed class GoogleSignInResult {
    data class Success(
        val email: String,
        val displayName: String,
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
}
