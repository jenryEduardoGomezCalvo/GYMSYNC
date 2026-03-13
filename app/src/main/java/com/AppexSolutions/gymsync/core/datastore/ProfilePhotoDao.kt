package com.AppexSolutions.gymsync.core.datastore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO para gestionar las fotos de perfil de clientes almacenadas localmente.
 * Usa REPLACE para que al actualizar la foto se sobreescriba el registro anterior.
 */
@Dao
interface ProfilePhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(photo: ClientProfilePhotoEntity)

    @Query("SELECT photo_local_uri FROM client_profile_photos WHERE client_id = :clientId LIMIT 1")
    suspend fun getPhotoUri(clientId: Int): String?

    @Query("DELETE FROM client_profile_photos WHERE client_id = :clientId")
    suspend fun deleteByClientId(clientId: Int)
}
