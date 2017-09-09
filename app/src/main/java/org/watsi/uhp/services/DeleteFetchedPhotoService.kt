package org.watsi.uhp.services

import org.watsi.uhp.database.PhotoDao
import org.watsi.uhp.managers.ExceptionManager

open class DeleteFetchedPhotoService : AbstractSyncJobService() {
    override fun performSync(): Boolean {
        val photosToDelete = PhotoDao.canBeDeleted()

        photosToDelete?.forEach {
            if (!it.delete(this)) {
                ExceptionManager.reportErrorMessage("Failed to delete photo at: " + it.url)
                it.deleted = true
                it.update()
            }
        }

        return true
    }
}
