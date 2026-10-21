    package com.jorge.cervelog.notificaciones

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Timestamp
import com.jorge.cervelog.MainActivity
import com.jorge.cervelog.R
import com.jorge.cervelog.data.model.Publicacion
import com.jorge.cervelog.data.repository.AuthRepository
import com.jorge.cervelog.data.repository.PublicacionRepository
import com.jorge.cervelog.data.repository.UbicacionRepository
import kotlinx.coroutines.flow.catch
import java.util.Locale

private const val CANAL = "cervezas_cercanas"
private const val RADIO_METROS = 5_000f

// Notificación local cuando otro usuario publica una cerveza cerca.
// Android corta la red a las apps en segundo plano: lo publicado mientras tanto se avisa al
// volver a abrir la app. Para avisar con la app cerrada haría falta FCM.
class AvisoCervezasCercanas(
    private val context: Context,
    private val publicacionRepository: PublicacionRepository = PublicacionRepository(),
    private val ubicacionRepository: UbicacionRepository = UbicacionRepository(context),
    private val authRepository: AuthRepository = AuthRepository(),
) {
    suspend fun escuchar() {
        crearCanal()
        val miUid = authRepository.currentUser?.uid
        publicacionRepository.observarNuevas(desde = Timestamp.now())
            // Si falla (p. ej. sin conexión) simplemente no hay avisos; no debe cerrar la app
            .catch { }
            .collect { publicacion ->
                if (publicacion.autorUid == miUid) return@collect
                val distancia = distanciaHasta(publicacion) ?: return@collect
                if (distancia <= RADIO_METROS) notificar(publicacion, distancia)
            }
    }

    private suspend fun distanciaHasta(publicacion: Publicacion): Float? {
        // Si el móvil aún no tiene ninguna guardada (p. ej. recién encendido), se pide una
        val yo = try {
            ubicacionRepository.obtenerUltimaUbicacion() ?: ubicacionRepository.obtenerUbicacionActual()
        } catch (e: SecurityException) {
            null
        } ?: return null
        val resultado = FloatArray(1)
        Location.distanceBetween(yo.latitude, yo.longitude, publicacion.latitud, publicacion.longitud, resultado)
        return resultado[0]
    }

    private fun crearCanal() {
        // HIGH para que aparezca como banner en la parte superior, también con la app abierta
        val canal = NotificationChannel(CANAL, "Cervezas cerca de ti", NotificationManager.IMPORTANCE_HIGH)
        canal.description = "Avisa cuando alguien registra una cerveza cerca de tu ubicación"
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    // Desde Android 13 hace falta permiso; areNotificationsEnabled() lo refleja en todas las versiones
    @SuppressLint("MissingPermission")
    private fun notificar(publicacion: Publicacion, distanciaMetros: Float) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val distancia = String.format(Locale.forLanguageTag("es-ES"), "%.1f km", distanciaMetros / 1000)
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("🍺 Nueva cerveza a $distancia")
            .setContentText("${publicacion.autorEmail} ha probado ${publicacion.nombre} ${"★".repeat(publicacion.puntuacion)}")
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        manager.notify(publicacion.id.hashCode(), notificacion)
    }
}
