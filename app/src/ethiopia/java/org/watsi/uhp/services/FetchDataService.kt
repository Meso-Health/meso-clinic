package org.watsi.uhp.services

import io.reactivex.Completable
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.usecases.FetchBillablesUseCase
import org.watsi.domain.usecases.FetchDiagnosesUseCase
import org.watsi.domain.usecases.FetchMembersUseCase
import org.watsi.domain.usecases.FetchOpenIdentificationEventsUseCase
import org.watsi.domain.usecases.FetchReturnedClaimsUseCase
import org.watsi.uhp.R
import javax.inject.Inject

class FetchDataService : BaseService() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var fetchBillablesUseCase: FetchBillablesUseCase
    @Inject lateinit var fetchDiagnosesUseCase: FetchDiagnosesUseCase
    @Inject lateinit var fetchReturnedClaimsUseCase: FetchReturnedClaimsUseCase
    @Inject lateinit var fetchIdentificationEventsUseCase: FetchOpenIdentificationEventsUseCase
    @Inject lateinit var fetchMembersUseCase: FetchMembersUseCase
    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock
    @Inject lateinit var okHttpClient: OkHttpClient

    override fun executeTasks(): Completable {
        return Completable.fromCallable {



            if (sessionManager.userHasPermission(SessionManager.Permissions.FETCH_BILLABLES) ||
                sessionManager.userHasPermission(SessionManager.Permissions.FETCH_DIAGNOSES)) {
                // We want to clear the cache when there are no billables or diagnoses on the device.
                // This scenario would happen when we migrate schema from version 1 to version 2, and as a result
                // all the models on the phone are deleted, but the e-tag is still stored in the OKHttpCache.
                val billableCount = billableRepository.count().blockingGet()
                val diagnosisCount = diagnosisRepository.count().blockingGet()
                if (billableCount == 0 || diagnosisCount == 0) {
                    okHttpClient.cache().evictAll()
                }
            }

            if (memberRepository.count().blockingGet() == 0) {
                // Similar to above, if the members get cleared because of a destructive migration (or any reason)
                // we want to make sure the fetch them all. Using the old page key would restrict us to only
                // fetching members that had been updated since the timestamp, but we want the full set.
                preferencesManager.updateMembersPageKey(null)
            }

            val billablesCompletable = if (sessionManager.userHasPermission(SessionManager.Permissions.FETCH_BILLABLES)) {
                fetchBillablesUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_billables_error_label)) }
            } else {
                Completable.complete()
            }

            val diagnosesCompletable = if (sessionManager.userHasPermission(SessionManager.Permissions.FETCH_DIAGNOSES)) {
                fetchDiagnosesUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_diagnoses_error_label)) }
            } else {
                Completable.complete()
            }

            val returnedClaimsCompletable = if (sessionManager.userHasPermission(SessionManager.Permissions.FETCH_RETURNED_CLAIMS)) {
                fetchReturnedClaimsUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_returned_claims_error_label)) }
            } else {
                Completable.complete()
            }

            val identificationEventsCompletable = if (sessionManager.userHasPermission(SessionManager.Permissions.FETCH_IDENTIFICATION_EVENTS)) {
                fetchIdentificationEventsUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_identification_events_error_label)) }
            } else {
                Completable.complete()
            }

            Completable.concatArray(
                billablesCompletable,
                diagnosesCompletable,
                returnedClaimsCompletable,
                identificationEventsCompletable,
                fetchMembersUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_members_error_label)) },
                Completable.fromAction {
                    if (getErrorMessages().isEmpty()) {
                        preferencesManager.updateDataLastFetched(clock.instant())
                    }
                }
            ).blockingAwait()
        }
    }
}
