package org.watsi.domain.repositories

import org.watsi.domain.entities.Photo
import java.util.UUID

interface PhotoRepository {
    fun find(id: UUID): Photo
    fun create(photo: Photo)
    fun canBeDeleted(): List<Photo>
    fun deleteLocalImage(photo: Photo): Boolean
}
