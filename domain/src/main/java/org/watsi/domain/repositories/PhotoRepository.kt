package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Photo
import java.util.UUID

interface PhotoRepository {
    fun find(id: UUID): Single<Photo>
    fun create(photo: Photo): Completable
    fun sync(deltas: List<Delta>): Completable
    fun deleteSynced(): Completable
}
