package com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model

data class GymDataResponse(
    val success: Boolean,
    val message: String?,
    val data: LoginDataDto?
)

data class LoginDataDto(
    val token: String,
    val user: UserDto?
)

data class UserDto(
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null,
    val fechaNacimiento: String? = null,
    val activo: Boolean = true,
    val rolId: Int? = null,
    val gymId: Int? = null,
    val rol: RolDto? = null,
    val gym: GymDto? = null
)

data class RolDto(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null
)

data class GymDto(
    val id: Int,
    val nombre: String,
    val ubicacion: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val activo: Boolean = true
)
