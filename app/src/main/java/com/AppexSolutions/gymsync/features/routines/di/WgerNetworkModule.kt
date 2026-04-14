package com.AppexSolutions.gymsync.features.routines.di

import com.AppexSolutions.gymsync.features.routines.data.remote.WgerApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WgerNetworkModule {

    @Provides
    @Singleton
    @Named("wger")
    fun provideWgerRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://wger.de/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    @Provides
    @Singleton
    fun provideWgerApiService(@Named("wger") retrofit: Retrofit): WgerApiService =
        retrofit.create(WgerApiService::class.java)
}
