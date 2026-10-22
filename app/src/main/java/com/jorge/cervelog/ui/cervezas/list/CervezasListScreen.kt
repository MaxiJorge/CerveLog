package com.jorge.cervelog.ui.cervezas.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.ui.components.AvatarCerveza
import com.jorge.cervelog.ui.components.BarraSuperior
import com.jorge.cervelog.ui.components.LoadingContent
import com.jorge.cervelog.ui.components.MessageContent
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.asMessage
import com.jorge.cervelog.ui.components.formatearAbv
import com.jorge.cervelog.ui.components.formatearFecha
import com.jorge.cervelog.ui.components.subtitulo
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme
import java.time.Instant

@Composable
fun CervezasListRoute(
    onCervezaClick: (String) -> Unit,
    onAgregarClick: () -> Unit,
    viewModel: CervezasViewModel = viewModel(factory = CervezasViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CervezasListScreen(state, onCervezaClick, onAgregarClick)
}

@Composable
fun CervezasListScreen(
    state: CervezasUiState,
    onCervezaClick: (String) -> Unit,
    onAgregarClick: () -> Unit,
) {
    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.mis_cervezas)) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.registrar_cerveza))
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            CervezasUiState.Loading -> LoadingContent(modifier)
            is CervezasUiState.Error -> MessageContent(
                emoji = "⚠️",
                titulo = stringResource(R.string.error_cargar_cervezas),
                descripcion = state.error.asMessage(),
                modifier = modifier,
            )
            is CervezasUiState.Success ->
                if (state.cervezas.isEmpty()) {
                    MessageContent(
                        emoji = "🍻",
                        titulo = stringResource(R.string.lista_vacia_titulo),
                        descripcion = stringResource(R.string.lista_vacia_descripcion),
                        modifier = modifier,
                    )
                } else {
                    CervezasList(state.cervezas, onCervezaClick, modifier)
                }
        }
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
        contentPadding = PaddingValues(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        items(cervezas, key = { it.id }) { cerveza ->
            CervezaItem(cerveza, onClick = { onCervezaClick(cerveza.id) })
        }
    }
}

@Composable
private fun CervezaItem(cerveza: Cerveza, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarCerveza(tamano = 48.dp, estilo = AppTheme.typography.titleLarge)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppTheme.spacing.lg),
            ) {
                Text(
                    cerveza.nombre,
                    style = AppTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitulo = subtitulo(cerveza.cerveceria, cerveza.estilo)
                if (subtitulo.isNotEmpty()) {
                    Text(
                        subtitulo,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(AppTheme.spacing.xs))
                RatingStars(cerveza.puntuacion)
            }
            Column(horizontalAlignment = Alignment.End) {
                cerveza.abv?.let {
                    Text(it.formatearAbv(), style = AppTheme.typography.labelLarge, color = AppTheme.colors.primary)
                }
                cerveza.fecha?.let {
                    Text(
                        it.formatearFecha(),
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CervezasListPreview() {
    CerveLogTheme {
        CervezasListScreen(
            state = CervezasUiState.Success(
                listOf(
                    Cerveza(id = "1", nombre = "Patagonia Amber Lager", cerveceria = "Patagonia", estilo = "Amber Lager", abv = 5.2, puntuacion = 4, fecha = Instant.now()),
                    Cerveza(id = "2", nombre = "Antares Kölsch", estilo = "Kölsch", abv = 5.0, puntuacion = 3, fecha = Instant.now()),
                ),
            ),
            onCervezaClick = {},
            onAgregarClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CervezasListErrorPreview() {
    CerveLogTheme {
        CervezasListScreen(CervezasUiState.Error(DomainError.SinConexion), onCervezaClick = {}, onAgregarClick = {})
    }
}
