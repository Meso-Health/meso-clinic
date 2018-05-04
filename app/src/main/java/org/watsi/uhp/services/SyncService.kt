package org.watsi.uhp.services

import android.app.job.JobParameters
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import org.watsi.domain.usecases.SyncPhotoUseCase

import javax.inject.Inject

class SyncService : DaggerJobService() {

    @Inject lateinit var syncMemberUseCase: SyncMemberUseCase
    @Inject lateinit var syncPhotoUseCase: SyncPhotoUseCase
    @Inject lateinit var syncIdentificationEventUseCase: SyncIdentificationEventUseCase
    @Inject lateinit var syncEncounterUseCase: SyncEncounterUseCase
    @Inject lateinit var syncEncounterFormUseCase: SyncEncounterFormUseCase

    override fun onStartJob(params: JobParameters?): Boolean {
        syncMemberUseCase.execute()
        syncPhotoUseCase.execute()
        syncIdentificationEventUseCase.execute()
        syncEncounterUseCase.execute()
        syncEncounterFormUseCase.execute()

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}
