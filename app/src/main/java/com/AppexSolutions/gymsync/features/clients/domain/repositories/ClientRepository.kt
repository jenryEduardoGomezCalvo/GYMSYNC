package com.AppexSolutions.gymsync.features.clients.domain.repositories

import android.net.Uri
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.Gym
import com.AppexSolutions.gymsync.features.clients.domain.entities.Rol

interface ClientRepository {
    /** Obtiene todos los usuarios con rolId=4 (clientes) */
    suspend fun getClients(): List<Client>

    /** Obtiene un usuario por ID */
    suspend fun getUserById(userId: Int): Client

    /** Crea un usuario nuevo (POST /users/ multipart/form-data) */
    suspend fun createUser(
        nombres: String, apellidos: String, email: String, password: String,
        telefono: String?, fechaNacimiento: String?, rolId: Int, gymId: Int?,
        profileImageUri: Uri? = null
    ): Client

    /** Actualiza un usuario (PUT /users/:id) */
    suspend fun updateUser(
        userId: Int, nombres: String?, apellidos: String?,
        email: String?, telefono: String?, fechaNacimiento: String?
    ): Client

    /** Elimina un usuario (DELETE /users/:id) */
    suspend fun deleteUser(userId: Int)

    /** Toggle activo/inactivo (PATCH /users/:id/toggle-active) */
    suspend fun toggleUserActive(userId: Int): Client

    /** Lista de roles (GET /roles/) */
    suspend fun getRoles(): List<Rol>

    /** Lista de gyms (GET /gyms/) */
    suspend fun getGyms(): List<Gym>
}
