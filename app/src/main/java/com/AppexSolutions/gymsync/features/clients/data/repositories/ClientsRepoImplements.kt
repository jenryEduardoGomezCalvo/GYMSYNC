package com.AppexSolutions.gymsync.features.clients.data.repositories

import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.mapper.toDomain
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.CreateUserRequest
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.UpdateUserRequest
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.Gym
import com.AppexSolutions.gymsync.features.clients.domain.entities.Rol
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class ClientsRepoImplements(
    private val gymApi: GymSyncAPI
) : ClientRepository {

    override suspend fun getClients(): List<Client> {
        val response = gymApi.getAllUsers()
        // Filtrar solo los que tienen rolId = 4 (clientes)
        return response.data
            .filter { it.rolId == 4 }
            .map { it.toDomain() }
    }

    override suspend fun getUserById(userId: Int): Client {
        val response = gymApi.getUserById(userId)
        return response.data.toDomain()
    }

    override suspend fun createUser(
        nombres: String, apellidos: String, email: String, password: String,
        telefono: String?, fechaNacimiento: String?, rolId: Int, gymId: Int?
    ): Client {
        val request = CreateUserRequest(
            nombres = nombres,
            apellidos = apellidos,
            email = email,
            password = password,
            telefono = telefono,
            fechaNacimiento = fechaNacimiento,
            rolId = rolId,
            gymId = gymId
        )
        val response = gymApi.createUser(request)
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun updateUser(
        userId: Int, nombres: String?, apellidos: String?,
        email: String?, telefono: String?, fechaNacimiento: String?
    ): Client {
        val request = UpdateUserRequest(
            nombres = nombres,
            apellidos = apellidos,
            email = email,
            telefono = telefono,
            fechaNacimiento = fechaNacimiento,
            activo = null
        )
        val response = gymApi.updateUser(userId, request)
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun deleteUser(userId: Int) {
        val response = gymApi.deleteUser(userId)
        if (!response.success) throw Exception(response.message)
    }

    override suspend fun toggleUserActive(userId: Int): Client {
        val response = gymApi.toggleUserActive(userId)
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun getRoles(): List<Rol> {
        return gymApi.getRoles().data.map { it.toDomain() }
    }

    override suspend fun getGyms(): List<Gym> {
        return gymApi.getGyms().data.map { it.toDomain() }
    }
}
