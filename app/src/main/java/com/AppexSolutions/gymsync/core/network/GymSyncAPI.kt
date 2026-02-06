package com.AppexSolutions.gymsync.core.network

import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.GymDataResponse
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface GymSyncAPI {

    @POST("login")
    suspend fun Login(
        @Body body : LoginRequest
    ): GymDataResponse


}