package com.AppexSolutions.gymsync.core.datastore

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla local para almacenar la ruta de la foto de perfil de cada cliente.
 * Solo guarda la ruta del archivo en almacenamiento interno, NO el blob.
 */
@Entity(tableName = "client_profile_photos")
data class ClientProfilePhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "client_id")
    val clientId: Int,

    @ColumnInfo(name = "photo_local_uri")
    val photoLocalUri: String
)
