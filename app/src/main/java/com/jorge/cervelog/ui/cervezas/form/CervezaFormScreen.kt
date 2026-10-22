package com.jorge.cervelog.ui.cervezas.form

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.ui.components.BarraSuperior
import com.jorge.cervelog.ui.components.BotonConCarga
import com.jorge.cervelog.ui.components.FotoCerveza
import com.jorge.cervelog.ui.components.LoadingContent
import com.jorge.cervelog.ui.components.MapaUbicacion
import com.jorge.cervelog.ui.components.MensajeSnackbar
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.rememberPedirUbicacion
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme
import java.io.File

@Composable
fun CervezaFormRoute(
    onBack: () -> Unit,
    onGuardada: () -> Unit,
    viewModel: CervezaFormViewModel = viewModel(factory = CervezaFormViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pedirUbicacion = rememberPedirUbicacion { concedido ->
        if (concedido) viewModel.onUsarUbicacionActual() else viewModel.onPermisoUbicacionDenegado()
    }

    val uriCamara = remember { uriParaCamara(context) }
    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { hecha ->
        if (hecha) viewModel.onFotoElegida(uriCamara.toString())
    }
    // El selector de fotos no necesita permisos
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoElegida(uri.toString())
    }

    // Una cerveza nueva se registra donde está el usuario
    LaunchedEffect(Unit) {
        if (!viewModel.esEdicion && state.form.ubicacion == null) pedirUbicacion()
    }
    LaunchedEffect(state.guardada) {
        if (state.guardada) onGuardada()
    }

    CervezaFormScreen(
        state = state,
        esEdicion = viewModel.esEdicion,
        onBack = onBack,
        onFormChange = viewModel::onFormChange,
        onUsarUbicacion = pedirUbicacion,
        onElegirEnMapa = viewModel::onUbicacionElegida,
        onHacerFoto = {
            try {
                camaraLauncher.launch(uriCamara)
            } catch (e: ActivityNotFoundException) {
                viewModel.onCamaraNoDisponible()
            }
        },
        onElegirDeGaleria = {
            galeriaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onGuardar = viewModel::onGuardar,
        onErrorMostrado = viewModel::onErrorMostrado,
    )
}

private fun uriParaCamara(context: Context): Uri {
    val archivo = File(context.cacheDir, "fotos/camara.jpg").apply { parentFile?.mkdirs() }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

@Composable
fun CervezaFormScreen(
    state: CervezaFormUiState,
    esEdicion: Boolean,
    onBack: () -> Unit,
    onFormChange: (CervezaForm) -> Unit,
    onUsarUbicacion: () -> Unit,
    onElegirEnMapa: (Coordenadas) -> Unit,
    onHacerFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
    onGuardar: () -> Unit,
    onErrorMostrado: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    MensajeSnackbar(state.error, snackbarHostState, onErrorMostrado)

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(if (esEdicion) R.string.editar_cerveza else R.string.nueva_cerveza),
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.cargando) {
            LoadingContent(Modifier.padding(padding))
        } else {
            CervezaFormContent(
                state = state,
                onFormChange = onFormChange,
                onUsarUbicacion = onUsarUbicacion,
                onElegirEnMapa = onElegirEnMapa,
                onHacerFoto = onHacerFoto,
                onElegirDeGaleria = onElegirDeGaleria,
                onGuardar = onGuardar,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun CervezaFormContent(
    state: CervezaFormUiState,
    onFormChange: (CervezaForm) -> Unit,
    onUsarUbicacion: () -> Unit,
    onElegirEnMapa: (Coordenadas) -> Unit,
    onHacerFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    val habilitado = !state.guardando
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        FormTextField(form.nombre, { onFormChange(form.copy(nombre = it)) }, stringResource(R.string.campo_nombre), habilitado)
        FormTextField(form.cerveceria, { onFormChange(form.copy(cerveceria = it)) }, stringResource(R.string.campo_cerveceria), habilitado)
        FormTextField(form.estilo, { onFormChange(form.copy(estilo = it)) }, stringResource(R.string.campo_estilo), habilitado)
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            FormTextField(
                form.abv, { onFormChange(form.copy(abv = it)) }, stringResource(R.string.campo_abv), habilitado,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )
            FormTextField(
                form.ibu, { onFormChange(form.copy(ibu = it)) }, stringResource(R.string.ibu), habilitado,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            stringResource(R.string.campo_puntuacion),
            style = AppTheme.typography.titleSmall,
            color = AppTheme.colors.onSurfaceVariant,
        )
        RatingStars(
            puntuacion = form.puntuacion,
            size = 36.dp,
            onChange = { onFormChange(form.copy(puntuacion = it)) },
            enabled = habilitado,
        )

        FotoInput(
            foto = form.foto,
            procesando = state.procesandoFoto,
            enabled = habilitado,
            onHacerFoto = onHacerFoto,
            onElegirDeGaleria = onElegirDeGaleria,
        )

        FormTextField(form.lugar, { onFormChange(form.copy(lugar = it)) }, stringResource(R.string.campo_lugar), habilitado)
        UbicacionInput(
            ubicacion = form.ubicacion,
            titulo = form.lugar.ifBlank { form.nombre },
            obteniendo = state.obteniendoUbicacion,
            enabled = habilitado,
            onUsarUbicacion = onUsarUbicacion,
            onElegirEnMapa = onElegirEnMapa,
        )
        FormTextField(
            form.notas, { onFormChange(form.copy(notas = it)) }, stringResource(R.string.notas_cata), habilitado,
            singleLine = false,
            imeAction = ImeAction.Default,
        )

        BotonConCarga(texto = stringResource(R.string.guardar), cargando = state.guardando, onClick = onGuardar)
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(
            capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences
            else KeyboardCapitalization.None,
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        modifier = modifier,
    )
}

@Composable
private fun UbicacionInput(
    ubicacion: Coordenadas?,
    titulo: String,
    obteniendo: Boolean,
    enabled: Boolean,
    onUsarUbicacion: () -> Unit,
    onElegirEnMapa: (Coordenadas) -> Unit,
) {
    OutlinedButton(
        onClick = onUsarUbicacion,
        enabled = enabled && !obteniendo,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (obteniendo) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(stringResource(R.string.usar_mi_ubicacion), modifier = Modifier.padding(start = AppTheme.spacing.sm))
    }
    Text(
        stringResource(R.string.tocar_mapa),
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colors.onSurfaceVariant,
    )
    MapaUbicacion(
        ubicacion = ubicacion,
        titulo = titulo,
        onClick = if (enabled) onElegirEnMapa else null,
    )
}

@Composable
private fun FotoInput(
    foto: String?,
    procesando: Boolean,
    enabled: Boolean,
    onHacerFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        OutlinedButton(
            onClick = onHacerFoto,
            enabled = enabled && !procesando,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.hacer_foto))
        }
        OutlinedButton(
            onClick = onElegirDeGaleria,
            enabled = enabled && !procesando,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.elegir_galeria))
        }
    }
    if (procesando) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    } else if (foto != null) {
        FotoCerveza(foto)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CervezaFormPreview() {
    CerveLogTheme {
        CervezaFormScreen(
            state = CervezaFormUiState(form = CervezaForm(nombre = "Quilmes Stout", abv = "4,8", puntuacion = 3)),
            esEdicion = false,
            onBack = {},
            onFormChange = {},
            onUsarUbicacion = {},
            onElegirEnMapa = {},
            onHacerFoto = {},
            onElegirDeGaleria = {},
            onGuardar = {},
            onErrorMostrado = {},
        )
    }
}
