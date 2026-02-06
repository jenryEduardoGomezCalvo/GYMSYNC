package com.AppexSolutions.gymsync.features.auth.data.repositories

import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.mapper.toDomain
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.mapper.toLoginRequest
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.User
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie

class GymRepositoriesImp(
    private val gymApi : GymSyncAPI
): GymSyncRepositorie {
    override suspend fun LoginUser(user: User): AuthSession {
        return gymApi
            .Login(user.toLoginRequest())
            .toDomain()
    }
}