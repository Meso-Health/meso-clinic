package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class FetchService : BaseService() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository

    override fun executeTasks(): Completable {
        return Completable.concatArray(
                memberRepository.fetch().onErrorComplete { setErrored(it) },
                billableRepository.fetch().onErrorComplete { setErrored(it) },
                diagnosisRepository.fetch().onErrorComplete { setErrored(it) },
                Completable.fromAction { if (getErrored()) { throw Exception() } }
        )
    }
}