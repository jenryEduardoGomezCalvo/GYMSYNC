package com.AppexSolutions.gymsync.features.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.notifications.domain.model.Announcement
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.MarkAllAnnouncementsAsReadUseCase
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.MarkAnnouncementAsReadUseCase
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.ObserveAnnouncementsUseCase
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.ObserveUnreadAnnouncementCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    observe: ObserveAnnouncementsUseCase,
    observeUnread: ObserveUnreadAnnouncementCountUseCase,
    private val markAsRead: MarkAnnouncementAsReadUseCase,
    private val markAllAsRead: MarkAllAnnouncementsAsReadUseCase
) : ViewModel() {

    val announcements: StateFlow<List<Announcement>> = observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> = observeUnread()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun onOpen(id: Int) = viewModelScope.launch { markAsRead(id) }

    fun markAll() = viewModelScope.launch { markAllAsRead() }
}

/**
 * ViewModel minimalista para que cualquier pantalla del cliente (Home, Planes, Rutinas…)
 * pueda observar el contador de no leídos y mostrar el badge en el BottomNav.
 */
@HiltViewModel
class UnreadAnnouncementsBadgeViewModel @Inject constructor(
    observeUnread: ObserveUnreadAnnouncementCountUseCase
) : ViewModel() {

    val unreadCount: StateFlow<Int> = observeUnread()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
