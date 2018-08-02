package org.watsi.uhp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.uganda.activity_photo.background_overlay
import kotlinx.android.synthetic.uganda.activity_photo.cancel
import kotlinx.android.synthetic.uganda.activity_photo.guide
import kotlinx.android.synthetic.uganda.activity_photo.photo_circle
import kotlinx.android.synthetic.uganda.activity_photo.photo_hint
import kotlinx.android.synthetic.uganda.activity_photo.photo_ring
import kotlinx.android.synthetic.uganda.activity_photo.preview_delete
import kotlinx.android.synthetic.uganda.activity_photo.preview_save
import kotlinx.android.synthetic.uganda.activity_photo.preview_view
import kotlinx.android.synthetic.uganda.activity_photo.texture
import kotlinx.android.synthetic.uganda.activity_photo.touch
import org.watsi.device.managers.CameraHelper
import org.watsi.device.managers.Logger
import org.watsi.uhp.R
import javax.inject.Inject

/**
 * Base activity for capturing photos
 *
 * Supports showing a preview surface, updating the exposure settings based on touch events
 * and capturing a single image
 *
 * Image handling logic is delegated to the implementer via the abstract processImage
 */
abstract class PhotoActivity : DaggerAppCompatActivity(), View.OnTouchListener, TextureView.SurfaceTextureListener {

    companion object {
        const val RESULT_NEEDS_PERMISSION = 1
        const val RESULT_CAMERA_ERROR = 2
        const val RESULT_FAILED_SAVING_PHOTO = 3
    }

    @Inject lateinit var logger: Logger
    private var processingImage = false
    private var imageBytes: ByteArray? = null
    private val touchIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val displayMetrics = DisplayMetrics()
    private lateinit var touchShutterAnimationIn: Animation
    private lateinit var touchShutterAnimationOut: Animation
    private lateinit var cameraHelper: CameraHelper

    /**
     * Implementation should handle processing the image byte array and then finish the activity
     */
    abstract fun processImage(bytes: ByteArray)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            cameraHelper = CameraHelper(
                    texture, getSystemService(CameraManager::class.java), Callback(), displayMetrics)
            touchIndicatorPaint.color = Color.WHITE
            touchIndicatorPaint.style = Paint.Style.STROKE
            touchShutterAnimationIn = AnimationUtils.loadAnimation(this, R.anim.pulse_in)
            touchShutterAnimationOut = AnimationUtils.loadAnimation(this, R.anim.pulse_out)
            texture.setOnTouchListener(this)
            texture.surfaceTextureListener = this
            photo_ring.setOnTouchListener { _, event ->
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    photo_circle.startAnimation(touchShutterAnimationIn)
                } else if (event?.action == MotionEvent.ACTION_UP) {
                    photo_circle.startAnimation(touchShutterAnimationOut)
                    try {
                        cameraHelper.capturePhoto(null)
                    } catch (exception: Exception) {
                        val message = exception.message
                        if (exception is RuntimeException && message != null && message.contains("TimeoutException")) {
                            logger.warning(exception)
                        } else {
                            logger.error(exception)
                        }
                        cameraHelper.capturePhoto(null)
                    }
                }
                true
            }
            preview_delete.setOnClickListener {
                hidePreview()
            }
            preview_save.setOnClickListener {
                if (!processingImage) {
                    processingImage = true
                    val bytes = imageBytes
                    if (bytes != null) {
                        processImage(bytes)
                    } else {
                        finishAsFailure(RESULT_FAILED_SAVING_PHOTO)
                    }
                }
            }
        } else {
            finishAsFailure(RESULT_NEEDS_PERMISSION)
        }
    }

    /**
     * Sets up the window layout to be full-screen with a semi-translucent navigation bar
     */
    private fun setupLayout() {
        // confusing syntax but this sets all of the flags at once
        val uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = uiFlags
        window.navigationBarColor = getColor(R.color.translucentOverlay)
        supportActionBar?.hide()
    }

    override fun onResume() {
        setupLayout()
        super.onResume()
    }

    override fun onPause() {
        if (cameraHelper.isStarted()) cameraHelper.release()
        super.onPause()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // only update exposure region is imageBytes is null because we don't want to adjust
        // when exposure region when preview is being displayed
        if (event?.action == MotionEvent.ACTION_DOWN && imageBytes == null && cameraHelper.isStarted()) {
            cameraHelper.updateExposure(event.x / texture.width, event.y / texture.height)
        }
        return false
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        // no-op
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        // no-op
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (!cameraHelper.isStarted()) cameraHelper.start()
    }

    protected fun finishAsFailure(failureCode: Int) {
        setResult(failureCode)
        finish()
    }

    private fun hidePreview() {
        togglePreviewViews(false)
        imageBytes = null
    }

    private fun showPreview(bytes: ByteArray) {
        togglePreviewViews(true)
        preview_view.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        imageBytes = bytes
    }

    private fun togglePreviewViews(showPreview: Boolean) {
        arrayOf(background_overlay, preview_view, preview_delete, preview_save).map {
            it.visibility = if (showPreview) View.VISIBLE else View.GONE }
        arrayOf(touch, photo_hint, guide, cancel, photo_ring, photo_circle).map {
            it.visibility = if (showPreview) View.GONE else View.VISIBLE }
    }

    inner class Callback : CameraHelper.Callback {
        override fun onCapture(imageBytes: ByteArray) {
            showPreview(imageBytes)
        }

        override fun onError(errorCode: Int) {
            finishAsFailure(RESULT_CAMERA_ERROR)
        }
    }
}
