package com.AppexSolutions.gymsync.features.clients.domain.repositories

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client

interface ClientRepository {
    suspend fun GetClients(): Client
}