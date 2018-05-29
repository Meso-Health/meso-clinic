package org.watsi.uhp.services

import android.app.job.JobParameters
import io.reactivex.Completable
import org.watsi.device.managers.Logger
import org.watsi.domain.usecases.SyncBillableUseCase
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberPhotoUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import javax.inject.Inject

class SyncService : DaggerJobService() {

    @Inject lateinit var syncMemberUseCase: SyncMemberUseCase
    @Inject lateinit var syncMemberPhotoUseCase: SyncMemberPhotoUseCase
    @Inject lateinit var syncIdentificationEventUseCase: SyncIdentificationEventUseCase
    @Inject lateinit var syncBillableUseCase: SyncBillableUseCase
    @Inject lateinit var syncEncounterUseCase: SyncEncounterUseCase
    @Inject lateinit var syncEncounterFormUseCase: SyncEncounterFormUseCase
    @Inject lateinit var logger: Logger

    override fun onStartJob(params: JobParameters?): Boolean {
        Completable.concatArray(
                syncMemberUseCase.execute(),
                syncMemberPhotoUseCase.execute(),
                syncIdentificationEventUseCase.execute(),
                syncBillableUseCase.execute(),
                syncEncounterUseCase.execute(),
                syncEncounterFormUseCase.execute()
        ).subscribe({
            jobFinished(params, false)
        }, {
            logger.error(it)
            jobFinished(params, true)
        })
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}
