package com.AppexSolutions.gymsync.features.auth.domain.repositories

import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.User
import com.AppexSolutions.gymsync.features.auth.domain.entities.RegisterUser // Asegúrate de importar esto

interface GymSyncRepositorie {
    suspend fun LoginUser(user: User): AuthSession

    // ESTA ES LA LÍNEA QUE FALTABA:
    suspend fun RegisterUser(registerUser: RegisterUser): String
}