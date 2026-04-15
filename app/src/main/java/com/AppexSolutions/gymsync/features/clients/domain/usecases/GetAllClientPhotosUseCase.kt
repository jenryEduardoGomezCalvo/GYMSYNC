package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import javax.inject.Inject

class GetAllClientPhotosUseCase @Inject constructor(
    private val profilePhotoDao: ProfilePhotoDao
) {
    suspend operator fun invoke(clientIds: List<Int>): Map<Int, String> {
        val photoMap = mutableMapOf<Int, String>()
        for (id in clientIds) {
            val url = profilePhotoDao.getPhotoUrl(id)
            if (url != null) {
                photoMap[id] = url
            } else {
                val localUri = profilePhotoDao.getPhotoUri(id)
                if (localUri != null) {
                    photoMap[id] = localUri
                }
            }
        }
        return photoMap
    }
}
