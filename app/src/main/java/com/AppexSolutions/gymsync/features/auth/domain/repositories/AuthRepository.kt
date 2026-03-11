package com.AppexSolutions.gymsync.features.auth.domain.repositories

import com.AppexSolutions.gymsync.core.datastore.UserEntity
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Llama a la API y guarda la sesión en Room. */
    suspend fun login(email: String, password: String): Result<AuthSession>

    /** Persiste la sesión del usuario en Room tras un login exitoso. */
    suspend fun saveUserSession(user: UserEntity)

    suspend fun getUserByEmail(email: String): UserEntity?

    /** Devuelve el último usuario que habilitó biometría (útil para auto-login). */
    suspend fun getLastBiometricUser(): UserEntity?

    suspend fun updateBiometricStatus(email: String, enabled: Boolean)

    /** Borra el token local (logout sin borrar datos biométricos). */
    suspend fun clearSession(email: String)

    /** Flow reactivo: se actualiza en tiempo real cuando cambia el usuario. */
    fun observeUser(email: String): Flow<UserEntity?>

    /** Retorna true si existe al menos un usuario con biometría habilitada en caché. */
    suspend fun hasBiometricSession(): Boolean

    /** Borra todos los tokens y limpia la sesión activa. */
    suspend fun logout()
}
