package org.watsi.uhp.services

import org.watsi.uhp.managers.ExceptionManager
import org.watsi.uhp.repositories.PhotoRepository
import javax.inject.Inject

open class DeleteFetchedPhotoService : AbstractSyncJobService() {

    @Inject lateinit var photoRepository: PhotoRepository

    override fun performSync(): Boolean {

        photoRepository.canBeDeleted().forEach {
            if (!it.delete(this)) {
                ExceptionManager.reportErrorMessage("Failed to delete photo at: " + it.url)
                it.deleted = true
            }
            photoRepository.update(it)
        }

        return true
    }
}
