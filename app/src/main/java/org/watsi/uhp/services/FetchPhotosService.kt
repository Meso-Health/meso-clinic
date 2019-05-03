package org.watsi.uhp.services

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.device.managers.PreferencesManager
import org.watsi.domain.usecases.FetchMembersPhotosUseCase
import org.watsi.uhp.R
import javax.inject.Inject

class FetchPhotosService : BaseService() {

    @Inject lateinit var fetchMembersPhotosUseCase: FetchMembersPhotosUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            fetchMembersPhotosUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_member_photos_error_label)) },
            Completable.fromAction {
                if (getErrorMessages().isEmpty()) {
                    preferencesManager.updatePhotoLastFetched(clock.instant())
                }
            }
        )
    }
}
