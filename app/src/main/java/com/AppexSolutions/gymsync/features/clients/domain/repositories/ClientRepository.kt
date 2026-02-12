package com.AppexSolutions.gymsync.features.clients.domain.repositories

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase

interface ClientRepository {
    suspend fun GetClients(): List<Client>
    suspend fun GetClientById(clientId:Int): Client

    suspend fun updateClient(Client: Client): Client

    suspend fun deleteClient(clientId:Int)
}