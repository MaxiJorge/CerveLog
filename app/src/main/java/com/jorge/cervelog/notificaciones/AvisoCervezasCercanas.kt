package com.jorge.cervelog.notificaciones

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jorge.cervelog.MainActivity
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.CervezaCercana
import com.jorge.cervelog.domain.usecase.ObservarCervezasCercanasUseCase
import com.jorge.cervelog.ui.components.formatearKm
import java.time.Instant

private const val CANAL = "cervezas_cercanas"

// En segundo plano Android corta la red, así que lo publicado se avisa al volver a la app.
// Para avisar con la app cerrada haría falta FCM.
class AvisoCervezasCercanas(
    private val context: Context,
    private val observarCervezasCercanas: ObservarCervezasCercanasUseCase,
) {
    suspend fun escuchar() {
        crearCanal()
        observarCervezasCercanas(desde = Instant.now()).collect(::notificar)
    }

    private fun crearCanal() {
        // Importancia alta para que salga como banner
        val canal = NotificationChannel(
            CANAL,
            context.getString(R.string.canal_cercanas),
            NotificationManager.IMPORTANCE_HIGH,
        )
        canal.description = context.getString(R.string.canal_cercanas_descripcion)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    // Desde Android 13 hace falta permiso
    @SuppressLint("MissingPermission")
    private fun notificar(cercana: CervezaCercana) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val publicacion = cercana.publicacion
        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(context.getString(R.string.aviso_titulo, cercana.distanciaMetros.formatearKm()))
            .setContentText(
                context.getString(
                    R.string.aviso_texto,
                    publicacion.autorEmail,
                    publicacion.nombre,
                    "★".repeat(publicacion.puntuacion),
                ),
            )
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        manager.notify(publicacion.id.hashCode(), notificacion)
    }
}
