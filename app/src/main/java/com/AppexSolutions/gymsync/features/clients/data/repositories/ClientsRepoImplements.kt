package com.AppexSolutions.gymsync.features.clients.data.repositories

import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.mapper.toDomain
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class ClientsRepoImplements(
    private val gymApi: GymSyncAPI
): ClientRepository{
    override suspend fun GetClients(): List<Client> {
        return gymApi.GetAllMembers()
            .data
            .map { it.toDomain() }
    }

    override suspend fun GetClientById(clientId: Int): Client {
        return gymApi.getClientById(clientId)
            .toDomain()

    }

    override suspend fun updateClient(Client: Client) : Client{
        return gymApi.updateClient(
            idClient = Client.id,client=Client
        )

    }

    override suspend fun deleteClient(clientId: Int) {
        TODO("Not yet implemented")
    }

}