package com.AppexSolutions.gymsync.features.clients.domain.usecases

import android.net.Uri
import com.AppexSolutions.gymsync.core.datastore.ClientProfilePhotoEntity
import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.core.storage.SupabaseStorageManager
import javax.inject.Inject

/**
 * Guarda la foto de perfil del cliente:
 * 1. Sube la imagen a Supabase Storage.
 * 2. Guarda la URL pública en Room.
 */
class SaveProfilePhotoUseCase @Inject constructor(
    private val supabaseStorageManager: SupabaseStorageManager,
    private val profilePhotoDao: ProfilePhotoDao
) {
    suspend operator fun invoke(clientId: Int, imageUri: Uri): Result<String> = try {
        val publicUrl = supabaseStorageManager.uploadProfilePhoto(clientId.toString(), imageUri)
        profilePhotoDao.insertOrReplace(
            ClientProfilePhotoEntity(clientId = clientId, photoUrl = publicUrl)
        )
        Result.success(publicUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
