package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import android.util.Log
import com.AppexSolutions.gymsync.core.datastore.AnnouncementType
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.features.notifications.domain.repository.AnnouncementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessFcmMessageUseCase @Inject constructor(
    private val userDao: UserDao,
    private val announcementRepository: AnnouncementRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "ProcessFcmMessage"
    }

    /**
     * Sincroniza asistencia recibida por FCM.
     * Se ejecuta en background sin mostrar notificación.
     * Guarda en Room si existe una tabla de asistencias registrada.
     */
    fun syncAttendance(data: Map<String, String>) {
        val clientId = data["clientId"] ?: return
        val timestamp = data["timestamp"]
        val gymId = data["gymId"]
        val sessionType = data["sessionType"] ?: "general"

        scope.launch {
            try {
                // TODO: Crear tabla de asistencias en Room para persistir localmente
                // Por ahora solo logueamos la recepción
                Log.d(TAG, """
                    Asistencia recibida:
                    - Cliente: $clientId
                    - Gym: $gymId
                    - Tipo: $sessionType
                    - Timestamp: $timestamp
                """.trimIndent())

                // Aquí se podría emitir un evento para actualizar la UI en tiempo real
                // si la app está abierta en la pantalla de asistencias
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando asistencia", e)
            }
        }
    }

    /**
     * Fuerza regeneración del QR del usuario.
     * Útil si el admin invalida QRs antiguos por seguridad.
     * La app debe invalidar su cache y generar uno nuevo al abrirse.
     */
    fun refreshQrCode(data: Map<String, String>) {
        val userId = data["userId"] ?: return
        val reason = data["reason"] ?: "security_update"

        scope.launch {
            try {
                // Aquí se invalidaría el QR cacheado
                // TODO: Implementar invalidación de cache de QR
                Log.d(TAG, "QR invalidado para usuario: $userId (razón: $reason)")
            } catch (e: Exception) {
                Log.e(TAG, "Error invalidando QR", e)
            }
        }
    }

    /**
     * Actualiza datos de membresía desde el servidor.
     * Se dispara cuando hay cambios en el plan del usuario.
     */
    fun updateMembership(data: Map<String, String>) {
        val userId = data["userId"] ?: return
        val membershipType = data["membershipType"]
        val expirationDate = data["expirationDate"]

        scope.launch {
            try {
                // Actualizar datos locales de membresía
                // TODO: Sincronizar con API para obtener datos actualizados
                Log.d(TAG, """
                    Membresía actualizada:
                    - Usuario: $userId
                    - Tipo: $membershipType
                    - Expira: $expirationDate
                """.trimIndent())
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando membresía", e)
            }
        }
    }

    /**
     * Maneja anuncios del gimnasio recibidos por FCM.
     * Persiste el anuncio en Room para que aparezca en la pantalla del cliente
     * y alimente el badge de no leídos en el BottomNav.
     *
     * Claves esperadas en `data`:
     *  - announcementTitle (obligatoria)
     *  - announcementBody  (obligatoria)
     *  - announcementType  (opcional: GENERAL | URGENTE | PROMOCION; default GENERAL)
     *  - sentAt            (opcional: epoch millis; default now)
     *  - sentBy            (opcional: nombre/email del admin; default "Gimnasio")
     */
    fun handleAnnouncement(data: Map<String, String>) {
        val title = data["announcementTitle"] ?: data["title"] ?: "Anuncio"
        val body = data["announcementBody"] ?: data["message"] ?: ""
        if (body.isBlank()) return

        val type = runCatching {
            AnnouncementType.valueOf(
                (data["announcementType"] ?: data["type"] ?: "GENERAL").uppercase()
            )
        }.getOrDefault(AnnouncementType.GENERAL)

        val sentAt = data["sentAt"]?.toLongOrNull() ?: System.currentTimeMillis()
        val sentBy = data["sentBy"] ?: "Gimnasio"

        scope.launch {
            try {
                announcementRepository.persistReceivedAnnouncement(
                    title = title,
                    message = body,
                    type = type,
                    sentAt = sentAt,
                    sentBy = sentBy
                )
                Log.d(TAG, "Anuncio persistido: $title ($type)")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando anuncio", e)
            }
        }
    }

    private fun parseTimestamp(timestamp: String?): Long {
        return timestamp?.let {
            try {
                java.time.Instant.parse(it).toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } ?: System.currentTimeMillis()
    }
}
