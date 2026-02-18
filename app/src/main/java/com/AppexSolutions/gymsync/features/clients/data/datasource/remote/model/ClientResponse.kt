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
    @SerializedName("fecha_nacimiento")
    val fechaNacimiento: String?,
    val activo: Boolean,
    @SerializedName("rol_id")
    val rolId: Int,
    val rol: RolDto?,
    @SerializedName("gym_id")
    val gymId: Int?,
    val gym: GymDto?,

    val createdAt: String?,
    val updatedAt: String?
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
    val email: String?,
    val activo: Boolean? // Agregado
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

data class CreateUserRequest(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val password: String,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("fecha_nacimiento")
    val fechaNacimiento: String?,

    @SerializedName("rol_id")
    val rolId: Int,

    @SerializedName("gym_id")
    val gymId: Int?,
    val activo: Boolean = true
)

data class UpdateUserRequest(
    val nombres: String?,
    val apellidos: String?,
    val email: String?,
    val telefono: String?,
    @SerializedName("fecha_nacimiento")  // <-- agregar esta línea
    val fechaNacimiento: String?,
    val activo: Boolean?
)
