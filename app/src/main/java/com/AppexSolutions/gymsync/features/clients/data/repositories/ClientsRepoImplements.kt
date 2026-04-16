package com.AppexSolutions.gymsync.features.clients.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.mapper.toDomain
import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.UpdateUserRequest
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.Gym
import com.AppexSolutions.gymsync.features.clients.domain.entities.Rol
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ClientsRepoImplements @Inject constructor(
    private val gymApi: GymSyncAPI,
    @ApplicationContext private val context: Context
) : ClientRepository {

    companion object {
        private const val TAG = "ClientsRepo"
    }

    private fun String.asTextPart() = toRequestBody("text/plain".toMediaTypeOrNull())

    override suspend fun getClients(): List<Client> {
        val response = gymApi.getAllUsers()
        return response.data
            .filter { it.rolId == 4 }
            .map { it.toDomain() }
    }

    override suspend fun getUserById(userId: Int): Client {
        val response = gymApi.getUserById(userId)
        return response.data.toDomain()
    }

    override suspend fun createUser(
        nombres: String, apellidos: String, email: String, password: String,
        telefono: String?, fechaNacimiento: String?, rolId: Int, gymId: Int?,
        profileImageUri: Uri?
    ): Client {
        val imagePart = profileImageUri?.let { uri ->
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("No se pudo leer la imagen seleccionada")
            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profile_image", "profile.jpg", requestBody)
        }

        val response = gymApi.createUser(
            nombres = nombres.asTextPart(),
            apellidos = apellidos.asTextPart(),
            email = email.asTextPart(),
            password = password.asTextPart(),
            rolId = rolId.toString().asTextPart(),
            gymId = gymId?.toString()?.asTextPart(),
            telefono = telefono?.asTextPart(),
            fechaNacimiento = fechaNacimiento?.asTextPart(),
            activo = "true".asTextPart(),
            profileImage = imagePart
        )
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun updateUser(
        userId: Int, nombres: String?, apellidos: String?,
        email: String?, telefono: String?, fechaNacimiento: String?
    ): Client {
        val request = UpdateUserRequest(
            nombres = nombres,
            apellidos = apellidos,
            email = email,
            telefono = telefono,
            fechaNacimiento = fechaNacimiento,
            activo = null
        )
        val response = gymApi.updateUser(userId, request)
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun deleteUser(userId: Int) {
        val response = gymApi.deleteUser(userId)
        if (!response.success) throw Exception(response.message)
    }

    override suspend fun toggleUserActive(userId: Int): Client {
        val response = gymApi.toggleUserActive(userId)
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun getRoles(): List<Rol> {
        val response = gymApi.getRoles()
        Log.d(TAG, "GET /roles/ → success=${response.success} count=${response.data.size}")
        return response.data.map { it.toDomain() }
    }

    override suspend fun getGyms(): List<Gym> {
        val response = gymApi.getGyms()
        Log.d(TAG, "GET /gyms/ → success=${response.success} count=${response.data.size}")
        return response.data.map { it.toDomain() }
    }
}
