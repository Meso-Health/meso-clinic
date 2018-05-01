package org.watsi.device.db.repositories

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.PhotoModel
import org.watsi.domain.entities.Photo
import org.watsi.domain.repositories.PhotoRepository
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class PhotoRepositoryImpl(private val photoDao: PhotoDao,
                          private val clock: Clock,
                          private val contentResolver: ContentResolver) : PhotoRepository {

    override fun find(id: UUID): Single<Photo> {
        return photoDao.find(id).map { it.toPhoto() }.subscribeOn(Schedulers.io())
    }

    override fun create(photo: Photo): Completable {
        return Completable.fromAction {
            photoDao.insert(PhotoModel.fromPhoto(photo, clock))
        }.subscribeOn(Schedulers.io())
    }

    private fun deleteLocalImage(photo: Photo): Boolean {
        val uri = Uri.parse(photo.url)
        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media._ID), null, null, null)
        var deleted = false
        if (cursor != null) {
            deleted = if (cursor.count > 0) contentResolver.delete(uri, null, null) > 0 else true
            cursor.close()
        }
        photoDao.update(PhotoModel.fromPhoto(photo.copy(deleted = deleted), clock))
        return deleted
    }

    override fun cleanSynced(): Completable {
        //photoDao.canBeDeleted().map { it.toPhoto() }
        // TODO: finish implementing
        return Completable.complete()
    }

    private fun localImageBytes(photo: Photo): ByteArray? {
        var iStream: InputStream? = null
        var byteStream: ByteArrayOutputStream? = null
        try {
            iStream = contentResolver.openInputStream(Uri.parse(photo.url))
            byteStream = ByteArrayOutputStream()
            iStream.copyTo(byteStream)
            return byteStream.toByteArray()
        } catch (e: IOException) {
            // TODO: handle exception
        } finally {
            try {
                if (iStream != null) iStream.close()
                if (byteStream != null) byteStream.close()
            } catch (e1: IOException) {
                // TODO: handle exception
            }
        }
        return null
    }
}
