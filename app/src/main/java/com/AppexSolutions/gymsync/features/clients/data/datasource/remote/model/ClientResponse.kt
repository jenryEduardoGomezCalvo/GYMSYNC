package com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

// ── Respuesta de lista: GET /users/ ──
data class ClientsListResponse(
    val success: Boolean,
    val message: String,
    val data: List<ClientDto>
)

// ── Respuesta de detalle: GET /users/:id ──
data class ClientDetailResponse(
    val success: Boolean,
    val message: String,
    val data: ClientDto
)

// ── Respuesta genérica (delete, toggle, create, update) ──
/*data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: ClientDto?
)*/

// ── DTO de usuario que viene de la API (con rol y gym anidados) ──
data class ClientDto(
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String?,
    val activo: Boolean,
    @SerializedName("rolId")
    val rolId: Int,
    val rol: RolDto?,
    @SerializedName("gymId")
    val gymId: Int?,
    val gym: GymDto?
)

data class RolDto(
    val id: Int,
    val nombre: String,
    val descripcion: String?
)

data class GymDto(
    val id: Int,
    val nombre: String,
    val ubicacion: String?,
    val telefono: String?,
    val email: String?
)

// ── Respuestas para roles y gyms ──
data class RolesListResponse(
    val success: Boolean,
    val message: String,
    val data: List<RolDto>
)

data class GymsListResponse(
    val success: Boolean,
    val message: String,
    val data: List<GymDto>
)

// ── Request para crear usuario: POST /users/ ──
data class CreateUserRequest(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val password: String,
    val telefono: String?,
    val fechaNacimiento: String?,
    val rolId: Int,
    val gymId: Int?,
    val activo: Boolean = true
)

// ── Request para actualizar usuario: PUT /users/:id ──
data class UpdateUserRequest(
    val nombres: String?,
    val apellidos: String?,
    val email: String?,
    val telefono: String?,
    val fechaNacimiento: String?,
    val activo: Boolean?
)
