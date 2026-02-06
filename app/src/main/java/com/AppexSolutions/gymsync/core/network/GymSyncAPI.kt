package com.AppexSolutions.gymsync.core.network

import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.GymDataResponse
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.ClientResponse
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GymSyncAPI {

    @POST("login")
    suspend fun Login(
        @Body body : LoginRequest
    ): GymDataResponse

    @GET("members")
    suspend fun GetAllMembers(): ClientResponse
}