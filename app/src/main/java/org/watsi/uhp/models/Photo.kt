package org.watsi.uhp.models

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import com.google.common.io.ByteStreams
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.managers.ExceptionManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

@DatabaseTable(tableName = Photo.TABLE_NAME)
open class Photo() : AbstractModel<Photo, UUID>() {

    companion object {
        const val TABLE_NAME = "photos"
        const val FIELD_NAME_ID = "id"
        const val FIELD_NAME_URL = "url"
        const val FIELD_NAME_DELETED = "deleted"
        const val CAPTURE_IMAGE_FILE_PROVIDER = BuildConfig.APPLICATION_ID + ".fileprovider"

        @JvmStatic
        @Throws(IOException::class)
        fun getUriFromProvider(filename: String, context: Context): Uri {
            val dir = File(context.filesDir, "images/")
            if (!dir.exists()) dir.mkdirs()
            val image = File(dir, filename)
            return FileProvider.getUriForFile(context, CAPTURE_IMAGE_FILE_PROVIDER, image)
        }
    }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    var id: UUID = UUID.randomUUID()

    @DatabaseField(columnName = FIELD_NAME_URL, canBeNull = false)
    var url: String? = null

    @DatabaseField(columnName = FIELD_NAME_DELETED, canBeNull = false)
    var deleted: Boolean = false

    constructor(uri: Uri) : this() {
        url = uri.toString()
    }

    @Throws(FileDeletionException::class)
    open fun delete(context: Context): Boolean {
        val uri = Uri.parse(url)
        val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media._ID), null, null, null)
        if (cursor != null) {
            deleted = if (cursor.count > 0) context.contentResolver.delete(uri, null, null) > 0 else true
        }
        if (deleted) update()
        return deleted
    }

    open fun bytes(context: Context): ByteArray? {
        var iStream: InputStream? = null
        var byteStream: ByteArrayOutputStream? = null
        try {
            iStream = context.contentResolver.openInputStream(Uri.parse(url))
            byteStream = ByteArrayOutputStream()
            ByteStreams.copy(iStream!!, byteStream)
            return byteStream.toByteArray()
        } catch (e: IOException) {
            ExceptionManager.reportException(e)
        } finally {
            try {
                if (iStream != null) iStream.close()
                if (byteStream != null) byteStream.close()
            } catch (e1: IOException) {
                ExceptionManager.reportException(e1)
            }
        }
        return null
    }

    class FileDeletionException(reason: String) : Exception(reason)
}
