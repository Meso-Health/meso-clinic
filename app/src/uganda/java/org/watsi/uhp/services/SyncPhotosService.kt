package org.watsi.uhp.services

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.device.managers.PreferencesManager
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncMemberPhotoUseCase
import org.watsi.uhp.R
import javax.inject.Inject

class SyncPhotosService : BaseService() {

    @Inject lateinit var syncMemberPhotoUseCase: SyncMemberPhotoUseCase
    @Inject lateinit var syncEncounterFormUseCase: SyncEncounterFormUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncMemberPhotoUseCase.execute { setError(it, getString(R.string.sync_member_photos_error_label)) },
            syncEncounterFormUseCase.execute { setError(it, getString(R.string.sync_encounter_forms_error_label)) },
            Completable.fromAction {
                if (getErrorMessages().isEmpty()) {
                    preferencesManager.updatePhotoLastSynced(clock.instant())
                }
            }
        )
    }
}
