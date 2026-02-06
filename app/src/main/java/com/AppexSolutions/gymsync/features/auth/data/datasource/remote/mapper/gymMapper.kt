package com.AppexSolutions.gymsync.features.auth.data.datasource.remote.mapper

import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.GymDataResponse
import com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model.LoginRequest
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.User

fun GymDataResponse.toDomain(): AuthSession{
    return AuthSession(
        token = this.token,
        id_user = this.id_user
    )
}

fun User.toLoginRequest(): LoginRequest{
    return LoginRequest(
        email = this.email,
        password = this.password
    )
}