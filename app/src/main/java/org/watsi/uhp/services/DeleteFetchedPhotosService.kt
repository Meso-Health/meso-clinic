package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.repositories.PhotoRepository
import javax.inject.Inject

class DeleteFetchedPhotosService : BaseService() {

    @Inject lateinit var photoRepository: PhotoRepository

    override fun executeTasks(): Completable {
        return photoRepository.deleteSynced()
    }
}
