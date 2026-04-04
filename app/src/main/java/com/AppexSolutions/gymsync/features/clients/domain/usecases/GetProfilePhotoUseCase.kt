package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.features.clients.data.datasource.hardware.ProfilePhotoManager
import javax.inject.Inject

/**
 * Consulta la foto de perfil local de un cliente.
 * Devuelve la ruta del archivo si existe en Room Y en disco, null si no.
 */
class GetProfilePhotoUseCase @Inject constructor(
    private val profilePhotoDao: ProfilePhotoDao,
    private val profilePhotoManager: ProfilePhotoManager
) {
    suspend operator fun invoke(clientId: Int): Result<String?> = try {
        val localPath = profilePhotoDao.getPhotoUri(clientId)
        // Verificar que el archivo realmente existe en disco
        val validPath = localPath?.takeIf { profilePhotoManager.photoExists(it) }
        Result.success(validPath)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
