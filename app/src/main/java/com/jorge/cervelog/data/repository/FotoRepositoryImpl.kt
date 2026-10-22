package com.jorge.cervelog.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import com.jorge.cervelog.data.mapper.resultadoDe
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.repository.FotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val LADO_MAXIMO = 800
private const val CALIDAD_JPEG = 75

class FotoRepositoryImpl(private val context: Context) : FotoRepository {

    override suspend fun procesarFoto(uri: String): AppResult<String> =
        when (val resultado = resultadoDe { fotoEnBase64(Uri.parse(uri)) }) {
            is AppResult.Success -> resultado.data?.let { AppResult.Success(it) } ?: AppResult.Error(DomainError.FotoNoValida)
            is AppResult.Error -> AppResult.Error(DomainError.FotoNoValida)
        }

    // Firestore limita los documentos a 1 MB
    private suspend fun fotoEnBase64(uri: Uri): String? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val tamano = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, tamano) }
        if (tamano.outWidth <= 0) return@withContext null

        // Se decodifica ya reducida para no cargar la foto entera en memoria
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

    // La cámara no gira la imagen, solo indica la orientación en el EXIF
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
