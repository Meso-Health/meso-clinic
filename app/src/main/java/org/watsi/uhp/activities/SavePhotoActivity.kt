package org.watsi.uhp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_photo.cancel_container
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Photo
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.helpers.BitmapHelper
import java.util.UUID
import javax.inject.Inject

class SavePhotoActivity : PhotoActivity() {

    @Inject lateinit var photoRepository: PhotoRepository

    companion object {
        const val PHOTO_ID_RESULT_KEY = "photo_id"
        const val CROPPED_PHOTO_ID_RESULT_KEY = "cropped_photo_id"
        const val CROP_HEIGHT = 240
        const val CROP_WIDTH = 240

        fun parseResult(resultCode: Int, data: Intent?, logger: Logger): Pair<Pair<UUID, UUID>?, String?> {
            return when (resultCode) {
                Activity.RESULT_OK -> {
                    val photoId = data?.getStringExtra(SavePhotoActivity.PHOTO_ID_RESULT_KEY)
                    val thumbnailPhotoId = data?.getStringExtra(SavePhotoActivity.CROPPED_PHOTO_ID_RESULT_KEY)
                    if (photoId != null && thumbnailPhotoId != null) {
                        Pair(Pair(UUID.fromString(photoId), UUID.fromString(thumbnailPhotoId)), null)
                    } else {
                        logger.error("SavePhotoActivity.parseResult called with non null photoId or thumbnailPhotoId",
                                hashMapOf("photoId" to photoId as String,
                                        "thumbnailPhotoId" to thumbnailPhotoId as String))
                        Pair(null, "failed")
                    }
                }
                else -> {
                    if (resultCode != Activity.RESULT_CANCELED) {
                        logger.error("SavePhotoActivity.parseResult called with resultCode: $resultCode")
                    }
                    Pair(null, "Failed")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cancel_container.setOnClickListener {
            finishAsFailure(RESULT_CANCELED)
        }
    }

    override fun processImage(bytes: ByteArray) {
        val photo = Photo(id = UUID.randomUUID(), bytes = bytes, url = null)
        val croppedPhoto = Photo(
                id = UUID.randomUUID(),
                bytes = BitmapHelper.cropByteArray(bytes, CROP_WIDTH, CROP_HEIGHT),
                url = null)

        Completable.concatArray(
                photoRepository.create(photo),
                photoRepository.create(croppedPhoto)
        ).subscribe(SavePhotoObserver(photo.id, croppedPhoto.id))
    }

    inner class SavePhotoObserver(private val photoId: UUID, private val croppedPhotoId: UUID) : CompletableObserver {
        override fun onComplete() {
            val resultIntent = Intent().apply {
                putExtra(PHOTO_ID_RESULT_KEY, photoId.toString())
                putExtra(CROPPED_PHOTO_ID_RESULT_KEY, croppedPhotoId.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        override fun onSubscribe(d: Disposable) {
            // no-op
        }

        override fun onError(e: Throwable) {
            logger.error(e)
            finishAsFailure(RESULT_FAILED_SAVING_PHOTO)
        }
    }
}
