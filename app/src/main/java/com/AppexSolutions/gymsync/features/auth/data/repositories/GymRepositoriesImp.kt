package com.AppexSolutions.gymsync.features.auth.data.repositories

import com.AppexSolutions.gymsync.core.datastore.AuthPreferences
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.mapper.toDomain
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.mapper.toLoginRequest
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.User
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie

class GymRepositoriesImp(
    private val gymApi : GymSyncAPI,
    private val AuthPreferences : AuthPreferences
): GymSyncRepositorie {
    override suspend fun LoginUser(user: User): AuthSession {
        val response = gymApi.Login(user.toLoginRequest())
        val session = response.toDomain()
        AuthPreferences.saveToken(session.token)
        return session
    }
}