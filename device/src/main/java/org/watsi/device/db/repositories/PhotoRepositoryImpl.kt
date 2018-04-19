package org.watsi.device.db.repositories

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import org.threeten.bp.Clock
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.PhotoModel
import org.watsi.domain.entities.Photo
import org.watsi.domain.repositories.PhotoRepository
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class PhotoRepositoryImpl(private val photoDao: PhotoDao,
                          private val clock: Clock,
                          private val contentResolver: ContentResolver) : PhotoRepository {

    override fun create(photo: Photo) {
        photoDao.insert(PhotoModel.fromPhoto(photo, clock))
    }

    override fun canBeDeleted(): List<Photo> {
        return photoDao.canBeDeleted().map { it.toPhoto() }
    }

    override fun deleteLocalImage(photo: Photo): Boolean {
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
