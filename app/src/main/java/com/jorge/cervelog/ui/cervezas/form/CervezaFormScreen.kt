package com.jorge.cervelog.ui.cervezas.form

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.ui.components.LoadingContent
import com.jorge.cervelog.ui.components.MapaUbicacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CervezaFormScreen(
    onBack: () -> Unit,
    onGuardada: () -> Unit,
    viewModel: CervezaFormViewModel = viewModel(factory = CervezaFormViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Vale tanto la ubicación precisa como la aproximada
    val permisoUbicacionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permisos ->
        if (permisos.values.any { it }) viewModel.usarUbicacionActual()
        else viewModel.permisoUbicacionDenegado()
    }
    val pedirUbicacion = {
        permisoUbicacionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }

    // Una cerveza nueva se registra donde está el usuario en ese momento
    LaunchedEffect(Unit) {
        if (!viewModel.esEdicion && state.form.latitud == null) pedirUbicacion()
    }

    LaunchedEffect(state.guardada) {
        if (state.guardada) onGuardada()
    }
    LaunchedEffect(state.error) {
        val texto = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(texto)
        viewModel.errorMostrado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.esEdicion) "Editar cerveza" else "Nueva cerveza") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.cargando) {
            LoadingContent(Modifier.padding(padding))
        } else {
            CervezaFormContent(
                form = state.form,
                guardando = state.guardando,
                obteniendoUbicacion = state.obteniendoUbicacion,
                onFormChange = viewModel::onFormChange,
                onUsarUbicacion = pedirUbicacion,
                onGuardar = viewModel::guardar,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun CervezaFormContent(
    form: CervezaForm,
    guardando: Boolean,
    obteniendoUbicacion: Boolean,
    onFormChange: (CervezaForm) -> Unit,
    onUsarUbicacion: () -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habilitado = !guardando
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FormTextField(form.nombre, { onFormChange(form.copy(nombre = it)) }, "Nombre *", habilitado)
        FormTextField(form.cerveceria, { onFormChange(form.copy(cerveceria = it)) }, "Cervecería", habilitado)
        FormTextField(form.estilo, { onFormChange(form.copy(estilo = it)) }, "Estilo (IPA, Stout, …)", habilitado)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FormTextField(
                form.abv, { onFormChange(form.copy(abv = it)) }, "ABV (%)", habilitado,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )
            FormTextField(
                form.ibu, { onFormChange(form.copy(ibu = it)) }, "IBU", habilitado,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            "Puntuación *",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RatingInput(
            puntuacion = form.puntuacion,
            onChange = { onFormChange(form.copy(puntuacion = it)) },
            enabled = habilitado,
        )

        FormTextField(form.lugar, { onFormChange(form.copy(lugar = it)) }, "Lugar (bar, casa, …)", habilitado)
        UbicacionInput(
            latitud = form.latitud,
            longitud = form.longitud,
            titulo = form.lugar.ifBlank { form.nombre },
            obteniendo = obteniendoUbicacion,
            enabled = habilitado,
            onUsarUbicacion = onUsarUbicacion,
        )
        FormTextField(
            form.notas, { onFormChange(form.copy(notas = it)) }, "Notas de cata", habilitado,
            singleLine = false,
            imeAction = ImeAction.Default,
        )

        Button(
            onClick = onGuardar,
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Guardar")
            }
        }
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
    latitud: Double?,
    longitud: Double?,
    titulo: String,
    obteniendo: Boolean,
    enabled: Boolean,
    onUsarUbicacion: () -> Unit,
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
        Text("Usar mi ubicación actual", modifier = Modifier.padding(start = 8.dp))
    }
    if (latitud != null && longitud != null) {
        MapaUbicacion(latitud = latitud, longitud = longitud, titulo = titulo)
    }
}

@Composable
private fun RatingInput(puntuacion: Int, onChange: (Int) -> Unit, enabled: Boolean) {
    Row {
        for (valor in 1..5) {
            IconButton(onClick = { onChange(valor) }, enabled = enabled) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$valor de 5",
                    modifier = Modifier.size(36.dp),
                    tint = if (valor <= puntuacion) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}
