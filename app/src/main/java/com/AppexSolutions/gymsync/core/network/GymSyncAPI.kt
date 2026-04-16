package com.AppexSolutions.gymsync.core.network




import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.GymDataResponse
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.*
import com.AppexSolutions.gymsync.features.notifications.data.remote.BroadcastRequestDto
import com.AppexSolutions.gymsync.features.notifications.data.remote.BroadcastResponseDto
import com.AppexSolutions.gymsync.features.notifications.data.remote.FcmTokenRequest
import com.AppexSolutions.gymsync.features.notifications.data.remote.FcmTokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @Multipart
    @POST("users/")
    suspend fun createUser(
        @Part("nombres") nombres: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("rol_id") rolId: RequestBody,
        @Part("gym_id") gymId: RequestBody?,
        @Part("telefono") telefono: RequestBody?,
        @Part("fecha_nacimiento") fechaNacimiento: RequestBody?,
        @Part("activo") activo: RequestBody?,
        @Part profileImage: MultipartBody.Part?
    ): ApiResponse<ClientDto>

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

    // ══════════════ FCM TOKEN ══════════════
    /**
     * Registra/actualiza el token FCM del usuario en el backend.
     * PATCH /users/{id}/fcm-token
     * Body: { "fcm_token": "..." }
     */
    @PATCH("users/{id}/fcm-token")
    suspend fun updateFcmToken(
        @Path("id") userId: Int,
        @Body body: FcmTokenRequest
    ): FcmTokenResponse

    // ══════════════ NOTIFICATIONS (BROADCAST FCM) ══════════════
    /**
     * Envía un anuncio broadcast a todos los usuarios del gimnasio del admin.
     * El backend resuelve destinatarios y tokens server-side.
     * Requiere JWT de admin/super_admin (inyectado vía AuthInterceptor).
     */
    @POST("notifications/broadcast")
    suspend fun broadcastAnnouncement(@Body body: BroadcastRequestDto): BroadcastResponseDto
}