package com.AppexSolutions.gymsync.features.auth.data.repositories

import com.AppexSolutions.gymsync.core.datastore.AuthPreferences
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.core.datastore.UserEntity
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val gymApi: GymSyncAPI,
    private val userDao: UserDao,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        return try {
            val response = gymApi.Login(LoginRequest(email, password))
            if (response.success) {
                val token = response.data?.token
                    ?: return Result.failure(Exception("Token no recibido"))
                val userDto = response.data.user
                val userId = userDto?.id ?: 0
                val name = buildString {
                    append(userDto?.nombres ?: "")
                    if (userDto?.apellidos?.isNotBlank() == true) append(" ${userDto.apellidos}")
                }

                // Guardar token en DataStore (para interceptor HTTP)
                authPreferences.saveToken(token)

                // Persistir en Room para sesiones futuras (biometría o caché)
                val existing = userDao.getUserByEmail(email)
                userDao.insert(
                    UserEntity(
                        id = existing?.id ?: 0,
                        email = email,
                        name = name.ifBlank { email },
                        token = token,
                        biometricEnabled = existing?.biometricEnabled ?: false,
                        lastLogin = Date()
                    )
                )

                Result.success(AuthSession(token = token, id_user = userId))
            } else {
                Result.failure(Exception(response.message ?: "Error de autenticación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserSession(user: UserEntity) {
        userDao.insert(user)
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    override suspend fun getLastBiometricUser(): UserEntity? {
        return userDao.getLastBiometricUser()
    }

    override suspend fun updateBiometricStatus(email: String, enabled: Boolean) {
        userDao.updateBiometricStatus(email, enabled)
    }

    override suspend fun clearSession(email: String) {
        userDao.clearSession(email)
        authPreferences.clearToken()
    }

    override fun observeUser(email: String): Flow<UserEntity?> {
        return userDao.observeUser(email)
    }

    override suspend fun hasBiometricSession(): Boolean {
        return userDao.countBiometricUsers() > 0
    }

    override suspend fun logout() {
        userDao.clearAllTokens()
        authPreferences.clearToken()
    }
}
