package com.AppexSolutions.gymsync.features.admin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.datastore.AnnouncementType
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.features.notifications.domain.model.BroadcastRecipient
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.GetBroadcastRecipientsUseCase
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.SendBroadcastAnnouncementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SendAnnouncementUiState(
    val title: String = "",
    val message: String = "",
    val type: AnnouncementType = AnnouncementType.GENERAL,
    val recipients: List<BroadcastRecipient> = emptyList(),
    val eligibleCount: Int = 0,
    val isLoadingRecipients: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val titleValid: Boolean get() = title.isNotBlank() && title.length <= 100
    val messageValid: Boolean get() = message.isNotBlank() && message.length <= 500
    val canSend: Boolean get() = titleValid && messageValid && !isSending
}

@HiltViewModel
class SendAnnouncementViewModel @Inject constructor(
    private val getRecipients: GetBroadcastRecipientsUseCase,
    private val sendBroadcast: SendBroadcastAnnouncementUseCase,
    private val userDao: UserDao
) : ViewModel() {

    private val _state = MutableStateFlow(SendAnnouncementUiState())
    val state: StateFlow<SendAnnouncementUiState> = _state.asStateFlow()

    init { loadRecipients() }

    fun loadRecipients() {
        _state.update { it.copy(isLoadingRecipients = true, errorMessage = null) }
        viewModelScope.launch {
            getRecipients().onSuccess { list ->
                _state.update {
                    it.copy(
                        recipients = list,
                        eligibleCount = list.count { r -> r.receivesNotifications },
                        isLoadingRecipients = false
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoadingRecipients = false,
                        errorMessage = e.message ?: "Error cargando destinatarios"
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        if (value.length <= 100) _state.update { it.copy(title = value) }
    }

    fun onMessageChange(value: String) {
        if (value.length <= 500) _state.update { it.copy(message = value) }
    }

    fun onTypeChange(type: AnnouncementType) = _state.update { it.copy(type = type) }

    fun dismissMessages() = _state.update { it.copy(errorMessage = null, successMessage = null) }

    fun send() {
        val s = _state.value
        if (!s.canSend) return
        _state.update { it.copy(isSending = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val sentBy = userDao.getActiveUser()?.let { it.name.ifBlank { it.email } } ?: "Admin"

            // El backend resuelve destinatarios y tokens server-side a partir del gym del admin.
            sendBroadcast(
                title = s.title.trim(),
                message = s.message.trim(),
                type = s.type,
                sentBy = sentBy
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        isSending = false,
                        title = "",
                        message = "",
                        type = AnnouncementType.GENERAL,
                        successMessage = "Enviado a ${result.sentCount} usuario(s)"
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isSending = false,
                        errorMessage = e.message ?: "No se pudo enviar el anuncio"
                    )
                }
            }
        }
    }
}
