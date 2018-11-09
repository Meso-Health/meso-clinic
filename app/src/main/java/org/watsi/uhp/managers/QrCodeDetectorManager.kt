package org.watsi.uhp.managers

import android.content.Context
import android.view.SurfaceHolder
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class QrCodeDetectorManager (context: Context) {

    private val barcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

    private val cameraSource = CameraSource.Builder(context, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedFps(15.0f)
            .setAutoFocusEnabled(true)
            .build()

    fun isOperational(): Boolean = barcodeDetector.isOperational

    @Throws(SecurityException::class)
    fun start(processor: Detector.Processor<Barcode>, holder: SurfaceHolder?) {
        barcodeDetector.setProcessor(processor)
        cameraSource.start(holder)
    }

    fun releaseResources() {
        try {
            cameraSource.release()
        } catch (e: NullPointerException) {
            // TODO: this is a hack
            // no-op
        }
        barcodeDetector.release()
    }
}
