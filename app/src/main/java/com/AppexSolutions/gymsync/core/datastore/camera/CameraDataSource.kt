package com.AppexSolutions.gymsync.core.datastore.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Data source para acceso a la cámara del dispositivo y
 * persistencia local de fotos de perfil.
 */
class CameraDataSource(private val context: Context) {

    companion object {
        private const val PROFILE_PHOTOS_DIR = "profile_photos"
    }

    /**
     * Crea un URI temporal para capturar una foto con la cámara.
     * El archivo se almacena en el directorio cache de la app.
     */
    fun createTempPhotoUri(): Uri {
        val tempFile = File.createTempFile(
            "profile_photo_",
            ".jpg",
            context.cacheDir
        ).apply { createNewFile() }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Copia la imagen seleccionada (galería o cámara) al almacenamiento
     * interno permanente de la app.
     *
     * @param sourceUri URI de la imagen seleccionada (temporal).
     * @param clientId ID del cliente para nombrar el archivo de forma única.
     * @return Ruta absoluta del archivo guardado permanentemente.
     */
    fun saveProfilePhoto(sourceUri: Uri, clientId: Int): String {
        val photosDir = File(context.filesDir, PROFILE_PHOTOS_DIR).apply {
            if (!exists()) mkdirs()
        }

        val destFile = File(photosDir, "client_${clientId}.jpg")

        // Si ya existe una foto anterior, la reemplaza
        if (destFile.exists()) destFile.delete()

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo leer la imagen seleccionada")

        return destFile.absolutePath
    }

    /**
     * Verifica si existe un archivo de foto local en la ruta dada.
     */
    fun photoExists(localPath: String): Boolean {
        return File(localPath).exists()
    }
}
