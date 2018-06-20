package org.watsi.domain.usecases

import io.reactivex.Single
import org.watsi.domain.entities.Photo
import org.watsi.domain.repositories.PhotoRepository
import java.util.UUID

class LoadPhotoUseCase(private val photoRepository: PhotoRepository) {

    fun execute(photoId: UUID) : Single<Photo> {
        return photoRepository.find(photoId)
    }
}
