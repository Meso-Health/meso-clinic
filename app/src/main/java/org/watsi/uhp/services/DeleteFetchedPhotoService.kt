package org.watsi.uhp.services

import org.watsi.domain.repositories.PhotoRepository
import javax.inject.Inject

open class DeleteFetchedPhotoService : AbstractSyncJobService() {

    @Inject lateinit var photoRepository: PhotoRepository

    override fun performSync(): Boolean {

        photoRepository.canBeDeleted().forEach {
            // TODO: handle case where it fails to delete
            photoRepository.deleteLocalImage(it)
        }

        return true
    }
}
