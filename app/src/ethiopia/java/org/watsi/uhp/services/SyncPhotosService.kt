package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.usecases.SyncMemberPhotoUseCase
import javax.inject.Inject

class SyncPhotosService : BaseService() {

    @Inject lateinit var syncMemberPhotoUseCase: SyncMemberPhotoUseCase

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncMemberPhotoUseCase.execute { setErrored(it) },
            Completable.fromAction { if (getErrored()) { throw Exception() } }
        )
    }
}
