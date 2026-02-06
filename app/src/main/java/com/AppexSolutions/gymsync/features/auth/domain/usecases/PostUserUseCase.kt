package com.AppexSolutions.gymsync.features.auth.domain.usecases

import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.User
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie

class PostUserUseCase(
    private val  repostory : GymSyncRepositorie
){
    suspend operator fun invoke(email: String, password: String):Result<AuthSession>{
        val user = User(
            email = email,
            password = password
        )
        val login = repostory.LoginUser(user)

        return Result.success(login)
    }
}