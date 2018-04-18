package org.watsi.uhp.repositories

import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.database.PhotoDao
import org.watsi.uhp.models.Photo

class PhotoRepositoryImpl : PhotoRepository {

    override fun create(photo: Photo) {
        DatabaseHelper.fetchDao(Photo::class.java).create(photo)
    }

    override fun update(photo: Photo) {
        DatabaseHelper.fetchDao(Photo::class.java).update(photo)
    }

    override fun canBeDeleted(): List<Photo> {
        return PhotoDao.canBeDeleted()
    }
}
