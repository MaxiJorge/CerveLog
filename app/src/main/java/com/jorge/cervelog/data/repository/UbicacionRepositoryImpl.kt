package com.jorge.cervelog.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jorge.cervelog.data.mapper.resultadoDe
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.repository.UbicacionRepository
import kotlinx.coroutines.tasks.await

class UbicacionRepositoryImpl(context: Context) : UbicacionRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // maxUpdateAge 0 evita la ubicación en caché; sin señal se rinde a los 15 s
    private val peticion = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMaxUpdateAgeMillis(0)
        .setDurationMillis(15_000)
        .build()

    @SuppressLint("MissingPermission")
    override suspend fun obtenerUbicacionActual(): AppResult<Coordenadas> =
        leer { fusedLocationClient.getCurrentLocation(peticion, null).await() }

    @SuppressLint("MissingPermission")
    override suspend fun obtenerUbicacionAproximada(): AppResult<Coordenadas> =
        leer { fusedLocationClient.lastLocation.await() ?: fusedLocationClient.getCurrentLocation(peticion, null).await() }

    // null si la ubicación está desactivada o no hay señal
    private suspend fun leer(lectura: suspend () -> Location?): AppResult<Coordenadas> =
        when (val resultado = resultadoDe(lectura)) {
            is AppResult.Success -> resultado.data?.let { AppResult.Success(Coordenadas(it.latitude, it.longitude)) }
                ?: AppResult.Error(DomainError.UbicacionNoDisponible)
            is AppResult.Error -> resultado
        }
}
