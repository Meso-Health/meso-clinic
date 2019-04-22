package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.usecases.SyncBillableUseCase
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import javax.inject.Inject

class SyncDataService : BaseService() {

    @Inject lateinit var syncMemberUseCase: SyncMemberUseCase
    @Inject lateinit var syncIdentificationEventUseCase: SyncIdentificationEventUseCase
    @Inject lateinit var syncBillableUseCase: SyncBillableUseCase
    @Inject lateinit var syncEncounterUseCase: SyncEncounterUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncMemberUseCase.execute { setError(it, "Upload Members") },
            syncIdentificationEventUseCase.execute { setError(it, "Upload Identifications") },
            syncBillableUseCase.execute { setError(it, "Upload Billables") },
            syncEncounterUseCase.execute { setError(it, "Upload Encounters") },
            Completable.fromAction {
                val errors = getErrorMessages()
                if (!errors.isEmpty()) {
                    throw ExecuteTasksFailureException()
                } else {
                    preferencesManager.updateDataLastSynced(clock.instant())
                }
            }
        )
    }
}
