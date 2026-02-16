package com.AppexSolutions.gymsync.features.auth.data.repositories

import com.AppexSolutions.gymsync.core.datastore.AuthPreferences
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.RegisterUser
import com.AppexSolutions.gymsync.features.auth.domain.entities.User
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie
// Importa el DTO correcto para la creación
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.CreateUserRequest

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
        // 1. Mapeamos los datos de la Entidad (Dominio) al Request de la API (Data)
        val request = CreateUserRequest(
            nombres = registerUser.nombres,
            apellidos = registerUser.apellidos,
            email = registerUser.email,
            password = registerUser.password,
            telefono = registerUser.telefono,
            fechaNacimiento = registerUser.fechaNacimiento,
            rolId = 4,
            gymId = null,
            activo = true
        )

        val response = gymApi.createUser(request)

        if (response.success) {
            return response.message
        } else {
            throw Exception(response.message)
        }
    }
}