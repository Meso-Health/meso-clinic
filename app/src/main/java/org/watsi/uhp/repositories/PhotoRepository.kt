package org.watsi.uhp.repositories

import org.watsi.uhp.models.Photo

interface PhotoRepository {
    fun create(photo: Photo)
    fun update(photo: Photo)
    fun canBeDeleted(): List<Photo>
}
