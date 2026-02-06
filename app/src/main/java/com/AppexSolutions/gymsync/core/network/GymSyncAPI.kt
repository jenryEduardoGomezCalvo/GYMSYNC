package com.AppexSolutions.gymsync.core.network

import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.GymDataResponse
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.ClientsListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GymSyncAPI {

    @POST("auth/login")
    suspend fun Login(
        @Body body : LoginRequest
    ): GymDataResponse

    @GET("users/")
    suspend fun GetAllMembers(): ClientsListResponse
}