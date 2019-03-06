package org.watsi.uhp.services

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.usecases.FetchReturnedClaimsUseCase
import javax.inject.Inject

class FetchService : BaseService() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository
    @Inject lateinit var fetchReturnedClaimsUseCase: FetchReturnedClaimsUseCase
    @Inject lateinit var okHttpClient: OkHttpClient

    override fun executeTasks(): Completable {
        return Completable.fromCallable {

            // We want to clear the cache when there are no billables or diagnoses on the device in order
            // to avoid a 304 from backend.
            // This scenario would happen when we migrate schema from version 1 to version 2, and as a result
            // all the models on the phone are deleted, but the e-tag is still stored in the OKHttpCache.
            val billableCount = billableRepository.count().blockingGet()
            val diagnosisCount = diagnosisRepository.count().blockingGet()
            if (billableCount == 0 || diagnosisCount == 0) {
                okHttpClient.cache().evictAll()
            }

            Completable.concatArray(
                billableRepository.fetch().onErrorComplete { setErrored(it) },
                diagnosisRepository.fetch().onErrorComplete { setErrored(it) },
                fetchReturnedClaimsUseCase.execute().onErrorComplete { setErrored(it) },
                memberRepository.fetch().onErrorComplete { setErrored(it) },
                Completable.fromAction { if (getErrored()) { throw Exception() } }
            ).blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
