package com.jorge.cervelog.ui.cervezas.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.data.model.Cerveza
import com.jorge.cervelog.ui.components.LoadingContent
import com.jorge.cervelog.ui.components.MessageContent
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.formatearAbv
import com.jorge.cervelog.ui.components.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CervezasListScreen(
    onCervezaClick: (String) -> Unit,
    onAgregarClick: () -> Unit,
    viewModel: CervezasViewModel = viewModel(factory = CervezasViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmarLogout by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis cervezas", fontWeight = FontWeight.Bold)
                        Text(
                            viewModel.emailUsuario,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { confirmarLogout = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarClick) {
                Icon(Icons.Filled.Add, contentDescription = "Registrar cerveza")
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (val s = state) {
            CervezasUiState.Loading -> LoadingContent(modifier)
            is CervezasUiState.Error -> MessageContent(
                emoji = "⚠️",
                titulo = s.mensaje,
                descripcion = "Revisa tu conexión e inténtalo de nuevo.",
                modifier = modifier,
            )
            is CervezasUiState.Success ->
                if (s.cervezas.isEmpty()) {
                    MessageContent(
                        emoji = "🍻",
                        titulo = "Todavía no has registrado ninguna cerveza",
                        descripcion = "Pulsa el botón + para registrar la primera.",
                        modifier = modifier,
                    )
                } else {
                    CervezasList(s.cervezas, onCervezaClick, modifier)
                }
        }
    }

    if (confirmarLogout) {
        AlertDialog(
            onDismissRequest = { confirmarLogout = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarLogout = false
                    viewModel.logout()
                }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLogout = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun CervezasList(
    cervezas: List<Cerveza>,
    onCervezaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(cervezas, key = { it.id }) { cerveza ->
            CervezaItem(cerveza, onClick = { onCervezaClick(cerveza.id) })
        }
    }
}

/** Tarjeta con la información resumida de una cerveza. */
@Composable
private fun CervezaItem(cerveza: Cerveza, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🍺", style = MaterialTheme.typography.titleLarge)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    cerveza.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitulo = listOf(cerveza.cerveceria, cerveza.estilo)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                if (subtitulo.isNotEmpty()) {
                    Text(
                        subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                RatingStars(cerveza.puntuacion)
            }
            Column(horizontalAlignment = Alignment.End) {
                cerveza.abv?.let {
                    Text(
                        it.formatearAbv(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                cerveza.fecha?.let {
                    Text(
                        it.formatearFecha(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
