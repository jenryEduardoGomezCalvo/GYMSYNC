package com.AppexSolutions.gymsync.features.admin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.notifications.domain.model.Announcement
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.ObserveAnnouncementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnnouncementHistoryViewModel @Inject constructor(
    observe: ObserveAnnouncementsUseCase
) : ViewModel() {

    val announcements: StateFlow<List<Announcement>> = observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
