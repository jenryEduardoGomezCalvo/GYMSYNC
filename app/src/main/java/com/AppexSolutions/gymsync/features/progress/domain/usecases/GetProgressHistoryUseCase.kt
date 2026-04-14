package com.AppexSolutions.gymsync.features.progress.domain.usecases

import com.AppexSolutions.gymsync.features.progress.domain.entities.ProgressEntry
import com.AppexSolutions.gymsync.features.progress.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProgressHistoryUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    operator fun invoke(userId: Int): Flow<List<ProgressEntry>> =
        repository.getHistory(userId)
}
