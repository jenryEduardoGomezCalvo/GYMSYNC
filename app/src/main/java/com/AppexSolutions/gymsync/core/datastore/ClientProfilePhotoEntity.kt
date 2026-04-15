package com.AppexSolutions.gymsync.core.datastore

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla local para almacenar la referencia de la foto de perfil de cada cliente.
 * photo_url: URL pública en Supabase Storage (fuente principal).
 * photo_local_uri: ruta local legacy (fallback para fotos anteriores a la migración).
 */
@Entity(tableName = "client_profile_photos")
data class ClientProfilePhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "client_id")
    val clientId: Int,

    @ColumnInfo(name = "photo_local_uri")
    val photoLocalUri: String = "",

    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null
)
