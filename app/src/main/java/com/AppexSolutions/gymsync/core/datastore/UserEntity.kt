package com.AppexSolutions.gymsync.core.datastore

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "token")
    val token: String,

    @ColumnInfo(name = "biometric_enabled")
    val biometricEnabled: Boolean = false,

    @ColumnInfo(name = "last_login")
    val lastLogin: Date = Date(),

    @ColumnInfo(name = "fcm_token")
    val fcmToken: String? = null,

    @ColumnInfo(name = "receives_notifications", defaultValue = "1")
    val receivesNotifications: Boolean = true,

    /** ID del usuario en el servidor (backend). Necesario para PATCH /users/{id}/fcm-token. */
    @ColumnInfo(name = "backend_id", defaultValue = "0")
    val backendId: Int = 0
)
