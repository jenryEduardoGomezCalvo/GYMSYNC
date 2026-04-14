package com.AppexSolutions.gymsync.features.users.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricAuthManager
import javax.inject.Inject

class HasMemberBiometricSessionUseCase @Inject constructor(
    private val userDao: UserDao,
    private val biometricAuthManager: BiometricAuthManager
) {
    suspend operator fun invoke(): Boolean {
        if (!biometricAuthManager.isHardwareAvailable()) return false
        if (!biometricAuthManager.isBiometricEnrolled()) return false
        return userDao.countBiometricUsers() > 0
    }
}
