package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.usecases.FetchReturnedClaimsUseCase
import javax.inject.Inject

class FetchService : BaseService() {

    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository
    @Inject lateinit var fetchReturnedClaimsUseCase: FetchReturnedClaimsUseCase

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            billableRepository.fetch().onErrorComplete { setErrored(it) },
            diagnosisRepository.fetch().onErrorComplete { setErrored(it) },
            fetchReturnedClaimsUseCase.execute().onErrorComplete { setErrored(it) },
            Completable.fromAction { if (getErrored()) { throw Exception() } }
        )
    }
}
