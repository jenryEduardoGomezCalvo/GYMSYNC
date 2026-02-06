package com.AppexSolutions.gymsync.features.clients.data.repositories

import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.mapper.toDomain
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class ClientsRepoImplements(
    private val gymApi: GymSyncAPI
): ClientRepository{
    override suspend fun GetClients(): Client {
        val response = gymApi.GetAllMembers()
        return response.toDomain()
    }

}