package com.AppexSolutions.gymsync.core.di

import android.content.Context
import com.AppexSolutions.gymsync.core.storage.SupabaseStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://nwgfrqhqtbcajudsoowa.supabase.co",
            supabaseKey = "sb_publishable_GGwiADpH9jO26sNBGg2M9Q_eH_sx334"
        ) {
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseStorageManager(
        supabaseClient: SupabaseClient,
        @ApplicationContext context: Context
    ): SupabaseStorageManager {
        return SupabaseStorageManager(supabaseClient, context)
    }
}
