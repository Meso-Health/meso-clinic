package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncMemberPhotoUseCase
import javax.inject.Inject

class SyncPhotosService : BaseService() {

    @Inject lateinit var syncMemberPhotoUseCase: SyncMemberPhotoUseCase
    @Inject lateinit var syncEncounterFormUseCase: SyncEncounterFormUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncMemberPhotoUseCase.execute { setError(it, "Upload Member Photos") },
            syncEncounterFormUseCase.execute { setError(it, "Upload Encounter Forms") },
            Completable.fromAction {
                val errors = getErrorMessages()
                if (!errors.isEmpty()) {
                    throw ExecuteTasksFailureException()
                } else {
                    preferencesManager.updatePhotoLastSynced(clock.instant())
                }
            }
        )
    }
}
