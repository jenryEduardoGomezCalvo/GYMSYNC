package com.AppexSolutions.gymsync.features.progress.domain.usecases

import com.AppexSolutions.gymsync.features.progress.domain.entities.ProgressEntry
import com.AppexSolutions.gymsync.features.progress.domain.repositories.ProgressRepository
import javax.inject.Inject

class AddProgressEntryUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(entry: ProgressEntry) = repository.addEntry(entry)
}
