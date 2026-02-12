package com.AppexSolutions.gymsync.core.network

import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.GymDataResponse
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.ClientDto
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.ClientsListResponse
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GymSyncAPI {

    @POST("auth/login")
    suspend fun Login(
        @Body body : LoginRequest
    ): GymDataResponse

    @GET("users/")
    suspend fun GetAllMembers(): ClientsListResponse

    @GET("users/{id}")
    suspend fun getClientById(
        @Path("id") idClient: Int
    ): ClientDto


    @PUT("users/{id}")
    suspend fun updateClient(
        @Path("id") idClient: Int,
        @Body client: Client
    ): Client
}