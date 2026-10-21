package com.jorge.cervelog.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class UbicacionRepository(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Por defecto puede devolver una ubicación en caché de hasta un minuto;
    // con maxUpdateAge = 0 siempre se pide una lectura nueva.
    // Si en 15 s no llega ninguna (sin señal GPS), se rinde y devuelve null.
    private val peticion = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMaxUpdateAgeMillis(0)
        .setDurationMillis(15_000)
        .build()

    // El permiso se pide en la pantalla (formulario o mapa) antes de llamar aquí.
    // Devuelve null si la ubicación del dispositivo está desactivada o no hay señal.
    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(): Location? =
        fusedLocationClient.getCurrentLocation(peticion, null).await()

    // Para saber si algo está a unos km basta la última conocida, sin esperar al GPS
    @SuppressLint("MissingPermission")
    suspend fun obtenerUltimaUbicacion(): Location? = fusedLocationClient.lastLocation.await()
}
