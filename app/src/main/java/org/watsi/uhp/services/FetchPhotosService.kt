package org.watsi.uhp.services

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.device.managers.PreferencesManager
import org.watsi.domain.usecases.FetchMembersPhotosUseCase
import javax.inject.Inject

class FetchPhotosService : BaseService() {

    @Inject lateinit var fetchMembersPhotosUseCase: FetchMembersPhotosUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            fetchMembersPhotosUseCase.execute().onErrorComplete { setError(it, "Download Member Photos") },
            Completable.fromAction {
                val errors = getErrorMessages()
                if (!errors.isEmpty()) {
                    throw ExecuteTasksFailureException()
                } else {
                    preferencesManager.updatePhotoLastFetched(clock.instant())
                }
            }
        )
    }
}
