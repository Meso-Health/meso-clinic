package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import org.watsi.domain.usecases.SyncPriceScheduleUseCase
import javax.inject.Inject

class SyncDataService : BaseService() {

    @Inject lateinit var syncMemberUseCase: SyncMemberUseCase
    @Inject lateinit var syncIdentificationEventUseCase: SyncIdentificationEventUseCase
    @Inject lateinit var syncEncounterUseCase: SyncEncounterUseCase
    @Inject lateinit var syncPriceScheduleUseCase: SyncPriceScheduleUseCase

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncMemberUseCase.execute { setErrored(it) },
            syncIdentificationEventUseCase.execute { setErrored(it) },
            syncPriceScheduleUseCase.execute { setErrored(it) },
            syncEncounterUseCase.execute { setErrored(it) },
            Completable.fromAction { if (getErrored()) { throw Exception() } }
        )
    }
}
