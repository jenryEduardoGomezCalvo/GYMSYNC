
package com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model

data class GymDataResponse(
    val success: Boolean,
    val message: String,
    val data: LoginDataDto
)

data class LoginDataDto(
    val token: String,
    val user: UserDto
)
data class UserDto(
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val email: String
)
