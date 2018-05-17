package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.PhotoModel
import org.watsi.domain.entities.Photo
import org.watsi.domain.repositories.PhotoRepository
import java.util.UUID

class PhotoRepositoryImpl(private val photoDao: PhotoDao,
                          private val clock: Clock) : PhotoRepository {

    override fun find(id: UUID): Single<Photo> {
        return photoDao.find(id).map { it.toPhoto() }.subscribeOn(Schedulers.io())
    }

    override fun create(photo: Photo): Completable {
        return Completable.fromAction {
            photoDao.insert(PhotoModel.fromPhoto(photo, clock))
        }.subscribeOn(Schedulers.io())
    }

    override fun deleteSynced(): Completable {
        return photoDao.canBeDeleted().flatMapCompletable {
            Completable.fromAction {
                it.forEach { model ->
                    photoDao.destroy(model)
                }
            }
        }.subscribeOn(Schedulers.io())
    }
}
