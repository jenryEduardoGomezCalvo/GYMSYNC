package com.AppexSolutions.gymsync.core.datastore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Consulta compleja: último usuario con biometría habilitada, ordenado por último login
    @Query(
        """
        SELECT * FROM users
        WHERE biometric_enabled = 1
        ORDER BY last_login DESC
        LIMIT 1
        """
    )
    suspend fun getLastBiometricUser(): UserEntity?

    @Query("UPDATE users SET biometric_enabled = :enabled WHERE email = :email")
    suspend fun updateBiometricStatus(email: String, enabled: Boolean)

    @Query("UPDATE users SET token = '' WHERE email = :email")
    suspend fun clearSession(email: String)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun observeUser(email: String): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users WHERE biometric_enabled = 1")
    suspend fun countBiometricUsers(): Int

    @Query("UPDATE users SET token = ''")
    suspend fun clearAllTokens()

    @Query("UPDATE users SET fcm_token = :fcmToken WHERE email = :email")
    suspend fun updateFcmToken(email: String, fcmToken: String)

    @Query("SELECT * FROM users WHERE fcm_token IS NOT NULL LIMIT 1")
    suspend fun getUserWithFcmToken(): UserEntity?
}
