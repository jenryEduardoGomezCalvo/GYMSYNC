package com.AppexSolutions.gymsync.features.users.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.UserDao
import javax.inject.Inject

class EnableMemberBiometricUseCase @Inject constructor(
    private val userDao: UserDao
) {
    suspend operator fun invoke(email: String, enable: Boolean = true) {
        userDao.updateBiometricStatus(email, enable)
    }
}
