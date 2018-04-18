package org.watsi.uhp.repositories

import org.watsi.uhp.database.PhotoDao
import org.watsi.uhp.models.Photo

class PhotoRepositoryImpl : PhotoRepository {
    override fun canBeDeleted(): List<Photo> {
        return PhotoDao.canBeDeleted()
    }
}
