package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import javax.inject.Inject

/**
 * Consulta la foto de perfil de un cliente.
 * Prioriza la URL de Supabase (photo_url). Si no existe, intenta la ruta local legacy.
 */
class GetProfilePhotoUseCase @Inject constructor(
    private val profilePhotoDao: ProfilePhotoDao
) {
    suspend operator fun invoke(clientId: Int): Result<String?> = try {
        val url = profilePhotoDao.getPhotoUrl(clientId)
        if (url != null) {
            Result.success(url)
        } else {
            val localPath = profilePhotoDao.getPhotoUri(clientId)
            Result.success(localPath)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
