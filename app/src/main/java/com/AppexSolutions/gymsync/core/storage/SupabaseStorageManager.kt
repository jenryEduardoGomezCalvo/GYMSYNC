package com.AppexSolutions.gymsync.core.storage

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStorageManager @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val context: Context
) {
    companion object {
        private const val BUCKET = "profile-photos"
    }

    suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): String {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("No se pudo leer la imagen")

        val path = "$userId.jpg"
        val bucket = supabaseClient.storage.from(BUCKET)

        bucket.upload(path, bytes) {
            upsert = true
        }

        return bucket.publicUrl(path)
    }

    fun getProfilePhotoUrl(userId: String): String {
        val path = "$userId.jpg"
        return supabaseClient.storage.from(BUCKET).publicUrl(path)
    }

    suspend fun deleteProfilePhoto(userId: String) {
        val path = "$userId.jpg"
        supabaseClient.storage.from(BUCKET).delete(path)
    }
}
