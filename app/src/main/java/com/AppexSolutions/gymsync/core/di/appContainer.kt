package com.AppexSolutions.gymsync.core.di

import android.content.Context
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.auth.data.repositories.GymRepositoriesImp
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie
import com.AppexSolutions.gymsync.features.clients.data.repositories.ClientsRepoImplements
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class appContainer(
    context : Context
) {

    private val gymsyncRetrofit : Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.100.11:3000/api/v1/auth/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    val gymApis: GymSyncAPI by lazy {
        gymsyncRetrofit.create(GymSyncAPI::class.java)
    }


    val GymRepositories : GymSyncRepositorie by lazy {
        GymRepositoriesImp(gymApis)
    }

    val ClientRepositorie : ClientRepository by lazy {
        ClientsRepoImplements(gymApis)
    }

}