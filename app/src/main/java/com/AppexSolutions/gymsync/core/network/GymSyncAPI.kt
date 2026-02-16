package com.AppexSolutions.gymsync.core.network




import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.GymDataResponse
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.*
import retrofit2.http.*

interface GymSyncAPI {

    // ══════════════ AUTH ══════════════
    @POST("auth/login")
    suspend fun Login(@Body body: LoginRequest): GymDataResponse

    // ══════════════ USERS ══════════════
    @GET("users/")
    suspend fun getAllUsers(): ClientsListResponse // O puedes usar ApiResponse<List<ClientDto>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): ClientDetailResponse // O ApiResponse<ClientDto>

    // AQUÍ ESTABA EL PROBLEMA: Agregamos <ClientDto>
    @POST("users/")
    suspend fun createUser(@Body body: CreateUserRequest): ApiResponse<ClientDto>

    // AQUÍ TAMBIÉN: <ClientDto>
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body body: UpdateUserRequest
    ): ApiResponse<ClientDto>

    // DELETE no devuelve data, usamos <Any> o <Unit>
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): ApiResponse<Any>

    // TOGGLE devuelve el usuario actualizado: <ClientDto>
    @PATCH("users/{id}/toggle-active")
    suspend fun toggleUserActive(@Path("id") id: Int): ApiResponse<ClientDto>

    // ══════════════ ROLES & GYMS ══════════════
    @GET("roles/")
    suspend fun getRoles(): RolesListResponse

    @GET("gyms/")
    suspend fun getGyms(): GymsListResponse
}