package org.watsi.domain.repositories

import org.watsi.domain.entities.Photo

interface PhotoRepository {
    fun create(photo: Photo)
    fun canBeDeleted(): List<Photo>
    fun deleteLocalImage(photo: Photo): Boolean
}
