package com.AppexSolutions.gymsync.core.di

import android.content.Context
import com.AppexSolutions.gymsync.core.datastore.AuthPreferences
import com.AppexSolutions.gymsync.core.network.AuthInterceptor
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.auth.data.repositories.GymRepositoriesImp
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie
import com.AppexSolutions.gymsync.features.clients.data.repositories.ClientsRepoImplements
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class appContainer(context: Context) {

    // 🔐 DataStore
    private val authPreferences = AuthPreferences(context)

    // 🔐 Interceptor con token
    private val authInterceptor = AuthInterceptor(authPreferences)

    // 🌐 OkHttp con interceptor
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    // 🔧 Retrofit
    private val gymsyncRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://98.80.61.113:3000/api/v1/")
        .client(okHttpClient) // ✅ AQUÍ VA EL TOKEN
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val gymApis: GymSyncAPI by lazy {
        gymsyncRetrofit.create(GymSyncAPI::class.java)
    }

    val gymRepositories: GymSyncRepositorie by lazy {
        GymRepositoriesImp(gymApis, authPreferences)
    }

    val clientRepository: ClientRepository by lazy {
        ClientsRepoImplements(gymApis)
    }
}
