package com.jorge.cervelog.ui.cervezas.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.data.model.Cerveza
import com.jorge.cervelog.ui.components.LoadingContent
import com.jorge.cervelog.ui.components.MapaUbicacion
import com.jorge.cervelog.ui.components.MessageContent
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.formatearAbv
import com.jorge.cervelog.ui.components.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CervezaDetailScreen(
    onBack: () -> Unit,
    onEditar: (String) -> Unit,
    viewModel: CervezaDetailViewModel = viewModel(factory = CervezaDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        (state as? CervezaDetailUiState.Success)?.cerveza?.nombre ?: "Detalle",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val cerveza = (state as? CervezaDetailUiState.Success)?.cerveza
                    if (cerveza != null) {
                        IconButton(onClick = { onEditar(cerveza.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (val s = state) {
            CervezaDetailUiState.Loading -> LoadingContent(modifier)
            CervezaDetailUiState.NotFound -> MessageContent(
                emoji = "🔍",
                titulo = "Esta cerveza ya no existe",
                descripcion = "Puede que se haya eliminado.",
                modifier = modifier,
                accion = "Volver",
                onAccion = onBack,
            )
            CervezaDetailUiState.Error -> MessageContent(
                emoji = "⚠️",
                titulo = "No pudimos cargar la cerveza",
                descripcion = "Revisa tu conexión e inténtalo de nuevo.",
                modifier = modifier,
                accion = "Volver",
                onAccion = onBack,
            )
            is CervezaDetailUiState.Success -> CervezaDetailContent(s.cerveza, modifier)
        }
    }
}

@Composable
private fun CervezaDetailContent(cerveza: Cerveza, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🍺", style = MaterialTheme.typography.displayLarge)
            Text(
                cerveza.nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (cerveza.cerveceria.isNotBlank()) {
                Text(
                    cerveza.cerveceria,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RatingStars(cerveza.puntuacion, size = 28.dp, modifier = Modifier.padding(top = 8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("ABV", cerveza.abv?.formatearAbv() ?: "—", Modifier.weight(1f))
            StatCard("IBU", cerveza.ibu?.toString() ?: "—", Modifier.weight(1f))
            StatCard("Puntuación", "${cerveza.puntuacion}/5", Modifier.weight(1f))
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp)) {
                InfoRow("Estilo", cerveza.estilo.ifBlank { "—" })
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow("Fecha", cerveza.fecha?.formatearFecha() ?: "—")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow("Lugar", cerveza.lugar.ifBlank { "—" })
            }
        }

        if (cerveza.latitud != null && cerveza.longitud != null) {
            MapaUbicacion(
                latitud = cerveza.latitud,
                longitud = cerveza.longitud,
                titulo = cerveza.lugar.ifBlank { cerveza.nombre },
            )
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    "Notas de cata",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    cerveza.notas.ifBlank { "Sin notas" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun InfoRow(label: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
