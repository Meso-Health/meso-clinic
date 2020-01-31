package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository

class ValidateDiagnosesAndBillablesExistenceUseCase(
    private val billableRepository: BillableRepository,
    private val diagnosisRepository: DiagnosisRepository
) {
    fun execute(): Completable {
        return Completable.fromCallable {
            val billableCount = billableRepository.countActive().blockingGet()
            val diagnosisCount = diagnosisRepository.countActive().blockingGet()
            if (billableCount == 0 || diagnosisCount == 0) {
                throw BillableAndDiagnosesMissingException("Either billables or diagnoses have not been downloaded yet.")
            }
        }.subscribeOn(Schedulers.io())
    }

    class BillableAndDiagnosesMissingException(msg: String): Exception(msg)
}
