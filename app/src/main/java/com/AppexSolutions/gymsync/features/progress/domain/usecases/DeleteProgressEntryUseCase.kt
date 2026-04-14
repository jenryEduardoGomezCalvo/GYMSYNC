package com.AppexSolutions.gymsync.features.progress.domain.usecases

import com.AppexSolutions.gymsync.features.progress.domain.repositories.ProgressRepository
import javax.inject.Inject

class DeleteProgressEntryUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteEntry(id)
}
