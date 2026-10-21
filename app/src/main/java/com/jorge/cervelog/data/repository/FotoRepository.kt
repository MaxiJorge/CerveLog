package com.jorge.cervelog.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

private const val LADO_MAXIMO = 800
private const val CALIDAD_JPEG = 75

class FotoRepository(private val context: Context) {

    // Fichero temporal donde la app de cámara deja la foto
    val uriCamara: Uri by lazy {
        val archivo = File(context.cacheDir, "fotos/camara.jpg").apply { parentFile?.mkdirs() }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    // Firestore admite documentos de hasta 1 MB, así que la foto se reduce y se comprime antes de guardarla
    suspend fun fotoEnBase64(uri: Uri): String? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val tamano = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, tamano) }
        if (tamano.outWidth <= 0) return@withContext null

        // Se decodifica ya reducida para no cargar en memoria la foto entera
        var muestreo = 1
        while (maxOf(tamano.outWidth, tamano.outHeight) / (muestreo * 2) >= LADO_MAXIMO) muestreo *= 2
        val original = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestreo })
        } ?: return@withContext null

        val escala = LADO_MAXIMO.toFloat() / maxOf(original.width, original.height)
        val matriz = Matrix().apply {
            if (escala < 1f) postScale(escala, escala)
            postRotate(rotacionExif(uri).toFloat())
        }
        val foto = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
        val bytes = ByteArrayOutputStream().use {
            foto.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, it)
            it.toByteArray()
        }
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // La cámara guarda la imagen sin girar e indica la orientación en los datos EXIF
    private fun rotacionExif(uri: Uri): Int {
        val orientacion = context.contentResolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
        return when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
