package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class GetClientsUsecase(
    private val repository : ClientRepository
){
    suspend operator fun invoke(): Result<Client>{
        val getClientsList = repository.GetClients()
        return Result.success(getClientsList)
    }
}