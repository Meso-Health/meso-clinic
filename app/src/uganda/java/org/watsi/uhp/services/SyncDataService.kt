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

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncMemberUseCase.execute { setErrored(it) },
            syncIdentificationEventUseCase.execute { setErrored(it) },
            syncBillableUseCase.execute { setErrored(it) },
            syncEncounterUseCase.execute { setErrored(it) },
            Completable.fromAction { if (getErrored()) { throw Exception() } }
        )
    }
}
