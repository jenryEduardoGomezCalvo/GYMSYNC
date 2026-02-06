package com.AppexSolutions.gymsync.features.auth.domain.repositories

import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.RegisterUser
import com.AppexSolutions.gymsync.features.auth.domain.entities.User

interface GymSyncRepositorie {

    suspend fun LoginUser(user: User): AuthSession
    suspend fun RegisterUser(registerUser: RegisterUser): String

}