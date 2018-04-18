package org.watsi.uhp.repositories

import org.watsi.uhp.models.Photo

interface PhotoRepository {
    fun canBeDeleted(): List<Photo>
}
