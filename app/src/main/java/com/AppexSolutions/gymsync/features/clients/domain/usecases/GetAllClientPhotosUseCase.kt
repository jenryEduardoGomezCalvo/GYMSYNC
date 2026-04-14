package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.features.clients.data.datasource.hardware.ProfilePhotoManager
import javax.inject.Inject

class GetAllClientPhotosUseCase @Inject constructor(
    private val profilePhotoDao: ProfilePhotoDao,
    private val profilePhotoManager: ProfilePhotoManager
) {
    suspend operator fun invoke(clientIds: List<Int>): Map<Int, String> {
        val photoMap = mutableMapOf<Int, String>()
        for (id in clientIds) {
            val uri = profilePhotoDao.getPhotoUri(id)
            if (uri != null && profilePhotoManager.photoExists(uri)) {
                photoMap[id] = uri
            }
        }
        return photoMap
    }
}
