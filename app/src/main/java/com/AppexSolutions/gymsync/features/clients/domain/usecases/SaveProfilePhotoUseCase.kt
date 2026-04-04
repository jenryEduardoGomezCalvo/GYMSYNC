package com.AppexSolutions.gymsync.features.clients.domain.usecases

import android.net.Uri
import com.AppexSolutions.gymsync.core.datastore.ClientProfilePhotoEntity
import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.features.clients.data.datasource.hardware.ProfilePhotoManager
import javax.inject.Inject

/**
 * Guarda la foto de perfil del cliente:
 * 1. Copia el archivo al almacenamiento interno permanente.
 * 2. Registra la ruta local en Room (reemplaza si ya existe).
 */
class SaveProfilePhotoUseCase @Inject constructor(
    private val profilePhotoManager: ProfilePhotoManager,
    private val profilePhotoDao: ProfilePhotoDao
) {
    suspend operator fun invoke(clientId: Int, imageUri: Uri): Result<String> = try {
        val localPath = profilePhotoManager.saveProfilePhoto(imageUri, clientId)
        profilePhotoDao.insertOrReplace(
            ClientProfilePhotoEntity(clientId = clientId, photoLocalUri = localPath)
        )
        Result.success(localPath)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
