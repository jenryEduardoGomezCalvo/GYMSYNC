package com.AppexSolutions.gymsync.core.network

import com.AppexSolutions.gymsync.core.datastore.AuthPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authPreferences: AuthPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Obtenemos el token de forma síncrona para OkHttp
        val token = runBlocking {
            authPreferences.token.firstOrNull()
        }

        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
