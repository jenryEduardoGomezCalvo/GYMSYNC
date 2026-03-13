package com.AppexSolutions.gymsync.features.clients.data.datasource.hardware

import android.net.Uri
import com.AppexSolutions.gymsync.core.datastore.camera.CameraDataSource

/**
 * Manager que orquesta el uso de la cámara/galería para fotos de perfil de clientes.
 * Usa el CameraDataSource del core para acceder al hardware.
 */
class ProfilePhotoManager(
    private val cameraDataSource: CameraDataSource
) {

    /**
     * Crea un URI temporal para capturar foto con la cámara.
     */
    fun createTempPhotoUri(): Uri {
        return cameraDataSource.createTempPhotoUri()
    }

    /**
     * Persiste la foto seleccionada en almacenamiento interno de la app.
     * Reemplaza la foto anterior si existe.
     *
     * @return Ruta absoluta del archivo guardado.
     */
    fun saveProfilePhoto(sourceUri: Uri, clientId: Int): String {
        return cameraDataSource.saveProfilePhoto(sourceUri, clientId)
    }

    /**
     * Verifica si existe una foto de perfil en la ruta dada.
     */
    fun photoExists(localPath: String): Boolean {
        return cameraDataSource.photoExists(localPath)
    }
}
