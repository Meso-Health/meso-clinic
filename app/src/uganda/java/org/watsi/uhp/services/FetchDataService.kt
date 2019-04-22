package org.watsi.uhp.services

import io.reactivex.Completable
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.device.managers.PreferencesManager
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.usecases.FetchBillablesUseCase
import org.watsi.domain.usecases.FetchDiagnosesUseCase
import org.watsi.domain.usecases.FetchMembersUseCase
import org.watsi.domain.usecases.FetchReturnedClaimsUseCase
import javax.inject.Inject

class FetchDataService : BaseService() {

    @Inject lateinit var fetchBillablesUseCase: FetchBillablesUseCase
    @Inject lateinit var fetchDiagnosesUseCase: FetchDiagnosesUseCase
    @Inject lateinit var fetchMembersUseCase: FetchMembersUseCase
    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock
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
                fetchBillablesUseCase.execute().onErrorComplete { setError(it, "Download Billables") },
                fetchDiagnosesUseCase.execute().onErrorComplete { setError(it, "Download Diagnoses") },
                fetchMembersUseCase.execute().onErrorComplete { setError(it, "Download Members") },
                Completable.fromAction {
                    val errors = getErrorMessages()
                    if (!errors.isEmpty()) {
                        throw ExecuteTasksFailureException()
                    } else {
                        preferencesManager.updateDataLastFetched(clock.instant())
                    }
                }
            ).blockingAwait()
        }
    }
}
