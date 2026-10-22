package com.jorge.cervelog.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jorge.cervelog.domain.model.DomainError
import kotlinx.coroutines.launch

// Abre el selector de cuentas de Google y devuelve el token que luego se canjea en Firebase
@Composable
fun rememberIniciarConGoogle(onToken: (String) -> Unit, onError: (DomainError) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val alObtenerToken by rememberUpdatedState(onToken)
    val alFallar by rememberUpdatedState(onError)

    return {
        scope.launch {
            val idCliente = idClienteWeb(context)
            if (idCliente == null) {
                alFallar(DomainError.GoogleNoConfigurado)
                return@launch
            }
            val peticion = GetCredentialRequest.Builder()
                .addCredentialOption(GetSignInWithGoogleOption.Builder(idCliente).build())
                .build()
            try {
                val credencial = CredentialManager.create(context).getCredential(context, peticion).credential
                if (credencial is CustomCredential &&
                    credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    alObtenerToken(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
                } else {
                    alFallar(DomainError.GoogleNoDisponible)
                }
            } catch (e: GetCredentialCancellationException) {
                // El usuario ha cerrado el selector
            } catch (e: GetCredentialException) {
                alFallar(DomainError.GoogleNoDisponible)
            }
        }
    }
}

// Lo genera google-services al activar Google en Firebase; se busca por nombre para compilar sin él
@SuppressLint("DiscouragedApi")
private fun idClienteWeb(context: Context): String? {
    val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    return if (id == 0) null else context.getString(id)
}
