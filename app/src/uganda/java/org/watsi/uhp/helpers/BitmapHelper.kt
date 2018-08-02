package org.watsi.uhp.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import java.io.ByteArrayOutputStream

object BitmapHelper {
    fun cropByteArray(bytes: ByteArray, width: Int, height: Int,
                      thumbnailExtractor: ThumbnailExtractor = ThumbnailExtractor,
                      stream: ByteArrayOutputStream= ByteArrayOutputStream()): ByteArray {
        val thumbnailBitmap = thumbnailExtractor.extractThumbnail(bytes, width, height)
        thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    object ThumbnailExtractor {
        fun extractThumbnail(bytes: ByteArray, width: Int, height: Int): Bitmap {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            return ThumbnailUtils.extractThumbnail(bitmap, width, height)
        }
    }
}
