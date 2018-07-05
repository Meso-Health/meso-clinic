package org.watsi.device.managers

import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.StreamConfigurationMap
import android.location.Location
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import java.nio.ByteBuffer

@RunWith(MockitoJUnitRunner::class)
class CameraHelperTest {
    @Rule @JvmField val rule = MockitoJUnit.rule()

    @Mock lateinit var mockTextureView: TextureView
    @Mock lateinit var mockCameraManager: CameraManager
    @Mock lateinit var mockCallback: CameraHelper.Callback
    @Mock lateinit var mockDisplayMetrics: DisplayMetrics
    @Mock lateinit var mockHandler: Handler
    @Mock lateinit var mockCameraDevice: CameraDevice
    @Mock lateinit var mockCameraCaptureSession: CameraCaptureSession
    @Mock lateinit var mockCaptureRequest: CaptureRequest
    @Mock lateinit var mockImageReader: ImageReader
    @Mock lateinit var mockLocation: Location
    val backFacingCameraId = "back"
    lateinit var spyCameraHelper: CameraHelper

    @Before
    fun setup() {
        // set DisplayMetrics dimensions based on actual screen dimensions of Moto G4
        mockDisplayMetrics.widthPixels = 1080
        mockDisplayMetrics.heightPixels = 1920
        mockCameraSettings()
        spyCameraHelper = spy(CameraHelper(
                mockTextureView, mockCameraManager, mockCallback, mockDisplayMetrics, mockHandler))
    }

    private fun mockCameraSettings() {
        whenever(mockCameraManager.cameraIdList).thenReturn(arrayOf("front", backFacingCameraId))
        val mockFrontCameraCharacteristics = buildMockCameraCharacteristics(
                mapOf(Pair(CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_FACING_FRONT)))
        val mockBackCameraCharacteristics = buildMockCameraCharacteristics(
                mapOf(Pair(CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_FACING_BACK)))
        whenever(mockCameraManager.getCameraCharacteristics("front"))
                .thenReturn(mockFrontCameraCharacteristics)
        whenever(mockCameraManager.getCameraCharacteristics(backFacingCameraId))
                .thenReturn(mockBackCameraCharacteristics)
    }

    private fun buildMockCameraCharacteristics(characteristics: Map<CameraCharacteristics.Key<*>, *>): CameraCharacteristics {
        val mockCameraCharacteristics = mock<CameraCharacteristics>()
        characteristics.forEach { whenever(mockCameraCharacteristics.get(it.key)).thenReturn(it.value) }
        return mockCameraCharacteristics
    }

    @Test
    fun init_selectsTheCorrectCameraId() {
        assertEquals(spyCameraHelper.cameraId, backFacingCameraId)
    }

    @Test
    fun start() {
        doNothing().whenever(spyCameraHelper).adjustTextureViewToCameraDimensions()

        spyCameraHelper.start()

        verify(spyCameraHelper).adjustTextureViewToCameraDimensions()
        verify(mockCameraManager).openCamera(eq(backFacingCameraId), any<CameraHelper.DeviceStateCallback>(), eq(mockHandler))
        assertTrue(spyCameraHelper.isStarted())
    }

    @Test
    fun adjustTextureViewToCameraDimensions() {
        val mockSize = mock<Rect> {
            on { width() } doReturn 4160
            on { height() } doReturn 3120
        }
        val mockCameraCharacteristics = buildMockCameraCharacteristics(
                mapOf(Pair(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, mockSize)))
        whenever(spyCameraHelper.cameraCharacteristics()).thenReturn(mockCameraCharacteristics)

        spyCameraHelper.adjustTextureViewToCameraDimensions()

        verify(spyCameraHelper).resizeTextureView(1440, 1920, -180f)
    }

    @Test
    fun startCaptureSession() {
        val mockStreamConfigurationMap = mock<StreamConfigurationMap>()
        val mockSize = mock<Size> {
            on { width } doReturn 1280
            on { height } doReturn 960
        }
        whenever(mockStreamConfigurationMap.getOutputSizes(ImageReader::class.java))
                .thenReturn(arrayOf(mockSize))
        val mockCameraCharacteristics = buildMockCameraCharacteristics(
                mapOf(Pair(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP, mockStreamConfigurationMap)))
        whenever(spyCameraHelper.cameraCharacteristics()).thenReturn(mockCameraCharacteristics)
        doReturn(mockImageReader).whenever(spyCameraHelper).buildImageReader(960, 1280)

        spyCameraHelper.startCaptureSession(mockCameraDevice)

        assertEquals(mockImageReader, spyCameraHelper.imageReader)
        assertNotNull(spyCameraHelper.surface)
        val captor = argumentCaptor<List<Surface>>()
        verify(mockCameraDevice).createCaptureSession(captor.capture(), any<CameraHelper.SessionStateCallback>(), eq(mockHandler))
        assertEquals(listOf(spyCameraHelper.surface, mockImageReader.surface), captor.firstValue)
    }

    @Test
    fun capturePhoto_notTakingPhoto_sessionAndImageReaderAreSet_initiatesStillCapture() {
        spyCameraHelper.session = mockCameraCaptureSession
        whenever(mockCameraCaptureSession.device).thenReturn(mockCameraDevice)
        spyCameraHelper.imageReader = mockImageReader
        whenever(mockImageReader.surface).thenReturn(mock())
        doReturn(mockCaptureRequest).whenever(spyCameraHelper).buildStillCaptureRequest(any(), any(), any())

        spyCameraHelper.capturePhoto(mockLocation)

        verify(spyCameraHelper).buildStillCaptureRequest(mockCameraDevice, mockImageReader.surface, mockLocation)
        verify(mockCameraCaptureSession).capture(eq(mockCaptureRequest), any<CameraHelper.CaptureCallback>(), eq(mockHandler))
    }

    @Test
    fun capturePhoto_notTakingPhoto_nullSession_callsOnErrorCallback() {
        spyCameraHelper.session = null

        spyCameraHelper.capturePhoto(mockLocation)

        verify(spyCameraHelper, never()).buildStillCaptureRequest(any(), any(), any())
        verify(mockCallback).onError(CameraHelper.ERROR_CAMERA_NOT_STARTED)
        assertTrue(spyCameraHelper.takingPhoto)
    }

    @Test
    fun capturePhoto_takingPhoto_returns() {
        spyCameraHelper.takingPhoto = true

        spyCameraHelper.capturePhoto(mockLocation)

        verify(spyCameraHelper, never()).buildStillCaptureRequest(any(), any(), any())
        verify(mockCallback, never()).onError(CameraHelper.ERROR_CAMERA_NOT_STARTED)
    }

    @Test
    fun buildStillCaptureRequest() {
        val mockSurface = mock<Surface>()
        val mockCaptureRequestBuilder = mock<CaptureRequest.Builder>()
        whenever(mockCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE))
                .thenReturn(mockCaptureRequestBuilder)
        whenever(mockCaptureRequestBuilder.build()).thenReturn(mockCaptureRequest)

        val request = spyCameraHelper.buildStillCaptureRequest(mockCameraDevice, mockSurface, mockLocation)

        assertEquals(mockCaptureRequest, request)
        verify(mockCaptureRequestBuilder).set(CaptureRequest.JPEG_QUALITY, 70)
        verify(mockCaptureRequestBuilder).set(CaptureRequest.JPEG_ORIENTATION, 90)
        verify(mockCaptureRequestBuilder).set(CaptureRequest.JPEG_GPS_LOCATION, mockLocation)
        verify(mockCaptureRequestBuilder).addTarget(mockSurface)
    }

    @Test
    fun updateExposure() {
        spyCameraHelper.session = mockCameraCaptureSession
        whenever(mockCameraCaptureSession.device).thenReturn(mockCameraDevice)
        val mockSize = mock<Rect> {
            on { width() } doReturn 3120
            on { height() } doReturn 4160
        }
        val mockCameraCharacteristics = buildMockCameraCharacteristics(
                mapOf(Pair(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, mockSize)))
        whenever(spyCameraHelper.cameraCharacteristics()).thenReturn(mockCameraCharacteristics)
        val mockCaptureRequestBuilder = mock<CaptureRequest.Builder>()
        whenever(mockCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW))
                .thenReturn(mockCaptureRequestBuilder)
        whenever(mockCaptureRequestBuilder.build()).thenReturn(mockCaptureRequest)
        val mockMeteringRectangle = mock<MeteringRectangle>()
        whenever(spyCameraHelper.buildMeteringRectangle(any(), any())).thenReturn(mockMeteringRectangle)

        spyCameraHelper.updateExposure(0.5f, 0.5f)

        verify(spyCameraHelper).buildMeteringRectangle(1410, 1930)
        verify(mockCaptureRequestBuilder).set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(mockMeteringRectangle))
        verify(mockCaptureRequestBuilder).addTarget(spyCameraHelper.surface)
        verify(mockCameraCaptureSession).setRepeatingRequest(mockCaptureRequest, null, mockHandler)
    }

    @Test
    fun release() {
        spyCameraHelper.session = mockCameraCaptureSession
        whenever(mockCameraCaptureSession.device).thenReturn(mockCameraDevice)
        spyCameraHelper.imageReader = mockImageReader
        spyCameraHelper.surface = mock()

        spyCameraHelper.release()

        verify(mockCameraCaptureSession).close()
        verify(mockCameraDevice).close()
        verify(mockImageReader).close()
        assertNull(spyCameraHelper.session)
        assertNull(spyCameraHelper.imageReader)
        assertNull(spyCameraHelper.surface)
        assertFalse(spyCameraHelper.isStarted())
    }

    @Test
    fun DeviceStateCallback_onOpened() {
        doNothing().whenever(spyCameraHelper).startCaptureSession(any())

        spyCameraHelper.DeviceStateCallback().onOpened(mockCameraDevice)

        verify(spyCameraHelper).startCaptureSession(mockCameraDevice)
    }

    @Test
    fun DeviceStateCallback_onDisconnected() {
        spyCameraHelper.DeviceStateCallback().onDisconnected(mockCameraDevice)

        verify(mockCameraDevice).close()
        verify(mockCallback).onError(CameraHelper.ERROR_CAMERA_DISCONNECTED)
    }

    @Test
    fun DeviceStateCallback_onError() {
        spyCameraHelper.DeviceStateCallback().onError(mockCameraDevice, 0)

        verify(mockCameraDevice).close()
        verify(mockCallback).onError(CameraHelper.ERROR_CAMERA_ERROR)
    }

    @Test
    fun SessionStateCallback_onConfigured() {
        val mockCaptureRequestBuilder = mock<CaptureRequest.Builder>()
        whenever(mockCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)).thenReturn(mockCaptureRequestBuilder)
        whenever(mockCaptureRequestBuilder.build()).thenReturn(mockCaptureRequest)

        spyCameraHelper.SessionStateCallback(mockCameraDevice).onConfigured(mockCameraCaptureSession)

        verify(mockCaptureRequestBuilder).addTarget(spyCameraHelper.surface)
        verify(mockCameraCaptureSession).setRepeatingRequest(mockCaptureRequest, null, mockHandler)
        assertEquals(mockCameraCaptureSession, spyCameraHelper.session)
    }

    @Test
    fun SessionStateCallback_onConfigureFailed() {
        spyCameraHelper.SessionStateCallback(mockCameraDevice).onConfigureFailed(mockCameraCaptureSession)

        verify(mockCallback).onError(CameraHelper.ERROR_CAPTURE_SESSION_CONFIG_ERROR)
    }

    @Test
    fun CaptureCallback_onCaptureCompleted() {
        spyCameraHelper.imageReader = mockImageReader
        val mockImage = mock<Image>()
        val mockPlane = mock<Image.Plane>()
        val bytes = ByteArray(1, { 0xa })
        val byteBuffer = ByteBuffer.wrap(bytes)
        whenever(mockImageReader.acquireLatestImage()).thenReturn(mockImage)
        whenever(mockImage.planes).thenReturn(arrayOf(mock(), mockPlane))
        whenever(mockPlane.buffer).thenReturn(byteBuffer)
        spyCameraHelper.takingPhoto = true

        spyCameraHelper.CaptureCallback().onCaptureCompleted(mock(), mock(), mock())

        verify(mockCallback).onCapture(bytes)
        verify(mockImage).close()
        assertFalse(spyCameraHelper.takingPhoto)
    }

    @Test
    fun CaptureCallback_onCaptureFailed() {
        spyCameraHelper.takingPhoto = true

        spyCameraHelper.CaptureCallback().onCaptureFailed(mock(), mock(), mock())

        verify(mockCallback).onError(CameraHelper.ERROR_CAPTURE_FAILED)
        assertFalse(spyCameraHelper.takingPhoto)
    }
}
