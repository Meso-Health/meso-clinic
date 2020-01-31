package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.repositories.DiagnosisRepository

class FetchDiagnosesUseCase(private val diagnosisRepository: DiagnosisRepository) {

    fun execute(): Completable {
        return diagnosisRepository.fetch()
    }
}
