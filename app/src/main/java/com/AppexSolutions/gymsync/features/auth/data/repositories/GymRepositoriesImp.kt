package com.AppexSolutions.gymsync.features.auth.data.repositories

import com.AppexSolutions.gymsync.core.datastore.AuthPreferences
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.RegisterUser
import com.AppexSolutions.gymsync.features.auth.domain.entities.User
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class GymRepositoriesImp(
    private val gymApi: GymSyncAPI,
    private val authPreferences: AuthPreferences
) : GymSyncRepositorie {

    override suspend fun LoginUser(user: User): AuthSession {
        val response = gymApi.Login(LoginRequest(user.email, user.password))
        if (response.success) {
            val token = response.data?.token ?: throw Exception("Token no recibido")
            authPreferences.saveToken(token)
            val userId = response.data.user?.id ?: 0
            return AuthSession(token = token, id_user = userId)
        } else {
            throw Exception(response.message ?: "Error de autenticación")
        }
    }

    override suspend fun RegisterUser(registerUser: RegisterUser): String {
        val asText = { s: String -> s.toRequestBody("text/plain".toMediaTypeOrNull()) }

        val response = gymApi.createUser(
            nombres = asText(registerUser.nombres),
            apellidos = asText(registerUser.apellidos),
            email = asText(registerUser.email),
            password = asText(registerUser.password),
            rolId = asText("4"),
            gymId = null,
            telefono = registerUser.telefono?.let { asText(it) },
            fechaNacimiento = registerUser.fechaNacimiento?.let { asText(it) },
            activo = asText("true"),
            profileImage = null
        )

        if (response.success) {
            return response.message
        } else {
            throw Exception(response.message)
        }
    }
}