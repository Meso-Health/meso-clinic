package org.watsi.device.managers

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.location.Location
import android.media.ImageReader
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout

/**
 * A helper class for wrapping the native CameraManager service in a simple API
 *
 * It provides functionality to preview images on a supplied SurfaceView,
 * capture photos as JPEGs, and release all resources
 *
 * Error handling and processing of the captured images is delegated to a callback interface
 */
class CameraHelper(
        private val cameraTextureView: TextureView,
        private val cameraManager: CameraManager,
        private val callback: Callback,
        private val displayMetrics: DisplayMetrics,
        private val handler: Handler = Handler() // TODO: tune Handler for this use-case
) {

    companion object {
        const val ERROR_CAMERA_DISCONNECTED = 0
        const val ERROR_CAMERA_ERROR = 1
        const val ERROR_CAMERA_NOT_STARTED = 2
        const val ERROR_INVALID_DIMENSIONS = 3
        const val ERROR_CAPTURE_SESSION_CONFIG_ERROR = 4
        const val ERROR_CAPTURE_FAILED = 5
        const val METERING_DIMENSION_SIZE = 300
    }

    // Android devices can have multiple cameras registered
    //  so this logic is used to always select the back-facing camera
    internal val cameraId = cameraManager.cameraIdList.find {
        cameraManager.getCameraCharacteristics(it)
                ?.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
    } ?: throw IllegalStateException("Device does not have a valid camera device")

    private var started = false
    internal var takingPhoto = false
    internal var surface: Surface? = null
    internal var session: CameraCaptureSession? = null
    internal var imageReader: ImageReader? = null

    /**
     * Acquires and initiates the camera resources and initiates the preview
     *
     * This should only be called after the supplied SurfaceView has been created
     */
    @Throws(SecurityException::class)
    fun start() {
        started = true
        adjustTextureViewToCameraDimensions()
        cameraManager.openCamera(cameraId, DeviceStateCallback(), handler)
    }

    fun isStarted(): Boolean = started

    /**
     * Adjusts the cameraTextureView width and position to handle any difference in aspect ratio
     * between the screen dimensions and the camera sensor dimensions
     *
     * This is necessary because the default behavior for differing aspect ratios is that the
     * preview image is stretched to fill the cameraTextureView. Instead we want to crop the
     * preview to fill the cameraTextureView so that it fills the screen but the aspect
     * ratio is maintained by performing a center crop.
     */
    internal fun adjustTextureViewToCameraDimensions() {
        cameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)?.let { sensorDimensions ->
            if (sensorDimensions.isEmpty || displayMetrics.heightPixels <= 0 || displayMetrics.widthPixels <= 0) {
                callback.onError(ERROR_INVALID_DIMENSIONS)
            } else {
                val adjustedWidth = adjustedTextureViewWidth(sensorDimensions.width(), sensorDimensions.height())
                resizeTextureView(adjustedWidth, displayMetrics.heightPixels, (displayMetrics.widthPixels - adjustedWidth) / 2f)
            }
        }
    }

    private fun adjustedTextureViewWidth(sensorPixelWidth: Int, sensorPixelHeight: Int): Int {
        // assumes aspect ratio of screen is more narrow than sensor
        val sensorAspectRatio = sensorPixelHeight.toFloat() / sensorPixelWidth.toFloat()
        val screenAspectRatio = displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()
        return (displayMetrics.widthPixels * sensorAspectRatio / screenAspectRatio).toInt()
    }

    internal fun resizeTextureView(width: Int, height: Int, x: Float) {
        cameraTextureView.translationX = x
        cameraTextureView.layoutParams = FrameLayout.LayoutParams(width, height)
    }

    /**
     * Creates an ImageReader that acts as a surface holder for captured images and starts
     * a CameraCaptureSession that is used both for previewing and capturing photos
     */
    internal fun startCaptureSession(cameraDevice: CameraDevice) {
        surface = Surface(cameraTextureView.surfaceTexture)
        val dimensions = cameraCharacteristics()
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(ImageReader::class.java).first()
        if (dimensions == null) {
            callback.onError(ERROR_CAMERA_ERROR)
        } else {
            val imageReader = buildImageReader(dimensions.height, dimensions.width)
            this.imageReader = imageReader
            cameraDevice.createCaptureSession(
                    listOf(surface, imageReader.surface),
                    SessionStateCallback(cameraDevice),
                    handler)
        }
    }

    /**
     * Captures the current image being displayed on the camera surface and calls the
     * CaptureCallback when it completes
     *
     * @param location GPS location that will be set in the captured JPEGs EXIF info
     */
    fun capturePhoto(location: Location?) {
        if (takingPhoto) return
        takingPhoto = true
        try {
            session!!.capture(
                    buildStillCaptureRequest(session!!.device, imageReader!!.surface, location),
                    CaptureCallback(),
                    handler)
        } catch (e: NullPointerException) {
            callback.onError(ERROR_CAMERA_NOT_STARTED)
        }
    }

    internal fun buildStillCaptureRequest(cameraDevice: CameraDevice, readerSurface: Surface, location: Location?): CaptureRequest {
        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)!!
        captureRequestBuilder.set(CaptureRequest.JPEG_QUALITY, 70)
        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90)
        location?.let { captureRequestBuilder.set(CaptureRequest.JPEG_GPS_LOCATION, location) }
        captureRequestBuilder.addTarget(readerSurface)
        return captureRequestBuilder.build()
    }

    /**
     * Updates the auto-exposure region to be a square of dimensions METERING_DIMENSION_SIZE
     * centered around the supplied parameters
     *
     * @param xPercent horizontal location from left as percentage of preview surface width
     * @param yPercent vertical location from top as percentage of preview surface height
     */
    fun updateExposure(xPercent: Float, yPercent: Float) {
        session?.let { session ->
            session.device?.let { cameraDevice ->
                cameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)?.let { dimensions ->
                    val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                    // yPercent is applied to sensor dimensions width and xPercent applied to height
                    // because x/yPercent are in terms of screen orientation which is 90 degrees
                    // rotated from the sensor orientation
                    val meteringRectangle = buildMeteringRectangle(
                            Math.max((dimensions.width() * yPercent).toInt() - (METERING_DIMENSION_SIZE / 2), 0),
                            Math.max((dimensions.height() * (1 - xPercent)).toInt() - (METERING_DIMENSION_SIZE / 2), 0))

                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(meteringRectangle))
                    captureRequestBuilder.addTarget(surface)
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
                }
            }
        }
    }

    internal fun buildMeteringRectangle(x: Int, y: Int): MeteringRectangle {
        return MeteringRectangle(x, y, METERING_DIMENSION_SIZE, METERING_DIMENSION_SIZE, 999)
    }

    /**
     * Frees all resources
     *
     * Should be called when the preview surface is no longer available, the host fragment/activity
     * is not active or after the required image has been captured and processed
     */
    fun release() {
        session?.close()
        session?.device?.close()
        session = null
        imageReader?.close()
        imageReader = null
        surface = null
        started = false
    }

    internal fun cameraCharacteristics(): CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)

    internal fun buildImageReader(width: Int, height: Int): ImageReader {
        // limit the ImageReader to 1 maxImage to limit memory usage and force us to clean-up
        // every image before capturing another
        return ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
    }

    interface Callback {
        fun onCapture(imageBytes: ByteArray)
        fun onError(errorCode: Int)
    }

    internal inner class DeviceStateCallback : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice?) {
            startCaptureSession(cameraDevice!!)
        }

        override fun onDisconnected(cameraDevice: CameraDevice?) {
            cameraDevice?.close()
            callback.onError(ERROR_CAMERA_DISCONNECTED)
        }

        override fun onError(cameraDevice: CameraDevice?, error: Int) {
            cameraDevice?.close()
            callback.onError(ERROR_CAMERA_ERROR)
        }
    }

    internal inner class SessionStateCallback(private val cameraDevice: CameraDevice) : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession?) {
            val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
            session = cameraCaptureSession
        }

        override fun onConfigureFailed(session: CameraCaptureSession?) {
            callback.onError(ERROR_CAPTURE_SESSION_CONFIG_ERROR)
        }
    }

    internal inner class CaptureCallback : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            imageReader?.acquireLatestImage()?.let {
                val imageBuffer = it.planes.last().buffer
                val bytes = ByteArray(imageBuffer.remaining())
                imageBuffer.get(bytes)
                callback.onCapture(bytes)
                it.close()
            }
            takingPhoto = false
        }

        override fun onCaptureFailed(session: CameraCaptureSession?, request: CaptureRequest?, failure: CaptureFailure?) {
            callback.onError(ERROR_CAPTURE_FAILED)
            takingPhoto = false
        }
    }
}
