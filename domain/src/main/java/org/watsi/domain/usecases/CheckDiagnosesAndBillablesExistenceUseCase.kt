package org.watsi.domain.usecases

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository

class CheckDiagnosesAndBillablesExistenceUseCase(
    private val billableRepository: BillableRepository,
    private val diagnosisRepository: DiagnosisRepository
) {
    fun execute(): Single<Boolean> {
        return Single.fromCallable {
            val billableCount = billableRepository.count().blockingGet()
            val diagnosisCount = diagnosisRepository.count().blockingGet()
            billableCount > 0 && diagnosisCount > 0
        }.subscribeOn(Schedulers.io())
    }
}
