package org.watsi.uhp.services

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import org.watsi.domain.usecases.SyncPriceScheduleUseCase
import org.watsi.uhp.R
import javax.inject.Inject

class SyncDataService : BaseService() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var syncMemberUseCase: SyncMemberUseCase
    @Inject lateinit var syncIdentificationEventUseCase: SyncIdentificationEventUseCase
    @Inject lateinit var syncEncounterUseCase: SyncEncounterUseCase
    @Inject lateinit var syncPriceScheduleUseCase: SyncPriceScheduleUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {

        val priceSchedulesCompletable = if (sessionManager.userHasPermission(SessionManager.Permissions.SYNC_PRICE_SCHEDULES)) {
            syncPriceScheduleUseCase.execute { setError(it, getString(R.string.sync_price_schedules_error_label)) }
        } else {
            Completable.complete()
        }

        return Completable.concatArray(
            syncMemberUseCase.execute { setError(it, getString(R.string.sync_members_error_label)) },
            syncIdentificationEventUseCase.execute { setError(it, getString(R.string.sync_id_events_error_label)) },
            priceSchedulesCompletable,
            syncEncounterUseCase.execute { setError(it, getString(R.string.sync_encounters_error_label)) },
            Completable.fromAction {
                if (getErrorMessages().isEmpty()) {
                    preferencesManager.updateDataLastSynced(clock.instant())
                }
            }
        )
    }
}
