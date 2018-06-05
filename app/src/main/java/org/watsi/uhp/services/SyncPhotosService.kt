package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncMemberPhotoUseCase
import javax.inject.Inject

class SyncPhotosService : BaseService() {

    @Inject lateinit var syncMemberPhotoUseCase: SyncMemberPhotoUseCase
    @Inject lateinit var syncEncounterFormUseCase: SyncEncounterFormUseCase

    override fun executeTasks(): Completable {
        return Completable.concatArray(
                syncMemberPhotoUseCase.execute().onErrorComplete { setErrored(it) },
                syncEncounterFormUseCase.execute().onErrorComplete { setErrored(it) },
                Completable.fromAction { if (getErrored()) { throw Exception() } }
        )
    }
}
