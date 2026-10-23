package com.jorge.cervelog.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.jorge.cervelog.data.mapper.RutaNoDisponibleException
import com.jorge.cervelog.data.mapper.resultadoDe
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.ModoTransporte
import com.jorge.cervelog.domain.model.Ruta
import com.jorge.cervelog.domain.repository.RutaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.time.Duration

private const val URL_ROUTES_API = "https://routes.googleapis.com/directions/v2:computeRoutes"
private const val CAMPOS = "routes.distanceMeters,routes.duration,routes.polyline.geoJsonLinestring"
private const val TIEMPO_ESPERA_MS = 10_000

// Routes API de Google con la misma key que el mapa
class RutaRepositoryImpl(private val context: Context, private val apiKey: String) : RutaRepository {

    // Si la key está restringida a apps Android, Google comprueba el paquete y la firma
    private val huellaFirma by lazy { huellaSha1(context) }

    override suspend fun calcularRuta(
        origen: Coordenadas,
        destino: Coordenadas,
        modo: ModoTransporte,
    ): AppResult<Ruta> = withContext(Dispatchers.IO) {
        resultadoDe {
            val conexion = (URL(URL_ROUTES_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = TIEMPO_ESPERA_MS
                readTimeout = TIEMPO_ESPERA_MS
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("X-Goog-Api-Key", apiKey)
                setRequestProperty("X-Goog-FieldMask", CAMPOS)
                setRequestProperty("X-Android-Package", context.packageName)
                setRequestProperty("X-Android-Cert", huellaFirma)
            }
            try {
                conexion.outputStream.use { it.write(peticion(origen, destino, modo).toString().toByteArray()) }
                if (conexion.responseCode != HttpURLConnection.HTTP_OK) throw RutaNoDisponibleException()
                val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
                JSONObject(respuesta).toRuta(modo) ?: throw RutaNoDisponibleException()
            } finally {
                conexion.disconnect()
            }
        }
    }
}

private fun peticion(origen: Coordenadas, destino: Coordenadas, modo: ModoTransporte) = JSONObject()
    .put("origin", origen.toPuntoRuta())
    .put("destination", destino.toPuntoRuta())
    .put("travelMode", if (modo == ModoTransporte.A_PIE) "WALK" else "DRIVE")
    // Devuelve los puntos tal cual en vez de codificados
    .put("polylineEncoding", "GEO_JSON_LINESTRING")

private fun Coordenadas.toPuntoRuta() = JSONObject().put(
    "location",
    JSONObject().put("latLng", JSONObject().put("latitude", latitud).put("longitude", longitud)),
)

// GeoJSON guarda cada punto como [longitud, latitud]
private fun JSONObject.toRuta(modo: ModoTransporte): Ruta? {
    val ruta = optJSONArray("routes")?.optJSONObject(0) ?: return null
    val coordenadas: JSONArray = ruta.optJSONObject("polyline")
        ?.optJSONObject("geoJsonLinestring")
        ?.optJSONArray("coordinates") ?: return null
    val puntos = (0 until coordenadas.length()).map { i ->
        val punto = coordenadas.getJSONArray(i)
        Coordenadas(latitud = punto.getDouble(1), longitud = punto.getDouble(0))
    }
    return Ruta(
        puntos = puntos,
        distanciaMetros = ruta.optInt("distanceMeters"),
        // Llega como "754s"
        duracion = Duration.ofSeconds(ruta.optString("duration").removeSuffix("s").toLongOrNull() ?: 0),
        modo = modo,
    )
}

@Suppress("DEPRECATION")
private fun huellaSha1(context: Context): String {
    val gestor = context.packageManager
    val firmas: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        gestor.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            .signingInfo?.apkContentsSigners
    } else {
        gestor.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
    }
    val firma = firmas?.firstOrNull() ?: return ""
    return MessageDigest.getInstance("SHA-1").digest(firma.toByteArray()).joinToString("") { "%02X".format(it) }
}
