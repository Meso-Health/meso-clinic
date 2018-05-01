package org.watsi.uhp.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.content.ContextCompat
import android.view.SurfaceHolder
import android.view.View
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_qr_code.cancel_container
import kotlinx.android.synthetic.main.activity_qr_code.scan_card_error
import kotlinx.android.synthetic.main.activity_qr_code.surface
import org.watsi.device.managers.Logger
import org.watsi.uhp.R
import org.watsi.uhp.managers.QrCodeDetectorManager
import java.util.concurrent.TimeUnit

abstract class QrCodeActivity : DaggerAppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var qrCodeDetectorManager: QrCodeDetectorManager

    companion object {
        const val RESULT_NEEDS_PERMISSION = 1
        const val RESULT_BARCODE_DETECTOR_NOT_OPERATIONAL = 2
        const val QR_CODE_RESULT_KEY = "qr_code"
        const val DURATION_TO_SHOW_ERROR_MESSAGES_IN_MS: Long = 1000
        const val MIN_TIME_BETWEEN_SCANS_IN_MS: Long = 1000
        const val CONFIRMATION_VIBRATION_LENGTH_IN_MS: Long = 150

        fun parseResult(resultCode: Int, data: Intent?, logger: Logger): Pair<String?, String?> {
            return when (resultCode) {
                Activity.RESULT_OK -> {
                    Pair(data?.getStringExtra(QrCodeActivity.QR_CODE_RESULT_KEY), null)
                }
                else -> {
                    if (resultCode != Activity.RESULT_CANCELED) {
                        logger.error("QrCodeActivity.parseResult called with resultCode: $resultCode")
                    }
                    Pair(null, "failed")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // make activity full-screen
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_qr_code)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            surface.holder.addCallback(this)
        } else {
            finishAsFailure(RESULT_NEEDS_PERMISSION)
        }

        cancel_container.setOnClickListener {
            finishAsFailure(RESULT_CANCELED)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        qrCodeDetectorManager = QrCodeDetectorManager(this)
        if (qrCodeDetectorManager.isOperational()) {
            qrCodeDetectorManager.start(QrCodeDetector(), holder)
        } else {
            finishAsFailure(RESULT_BARCODE_DETECTOR_NOT_OPERATIONAL)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        // no-op
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        qrCodeDetectorManager.releaseResources()
    }

    abstract fun onDetectedQrCode(qrCode: String)

    fun finishAsFailure(failureCode: Int) {
        val resultIntent = Intent()
        setResult(failureCode, resultIntent)
        finish()
    }

    fun setErrorMessage(message: String) {
        Completable.fromAction {
            scan_card_error.text = message
            scan_card_error.visibility = View.VISIBLE
        }
        .delay(DURATION_TO_SHOW_ERROR_MESSAGES_IN_MS, TimeUnit.MILLISECONDS)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            scan_card_error.visibility = View.GONE
        }
    }

    inner class QrCodeDetector : Detector.Processor<Barcode> {
        override fun release() {
            // no-op
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            // return first QR code detected as the result of the activity
            val codes = detections?.detectedItems
            if (codes != null && codes.size() > 0) {
                val qrCode = codes.valueAt(0).displayValue
                if (!isFinishing) {
                    onDetectedQrCode(qrCode)
                    Thread.sleep(MIN_TIME_BETWEEN_SCANS_IN_MS)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(CONFIRMATION_VIBRATION_LENGTH_IN_MS)
    }
}
