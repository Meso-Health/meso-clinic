package org.watsi.device.managers

import android.content.Intent
import android.support.v4.app.Fragment
import java.util.UUID

interface FingerprintManager {
    fun captureFingerprint(memberId: String, fragment: Fragment, requestCode: Int): Boolean
    fun parseResponse(resultCode: Int, data: Intent?): FingerprintResponse

    data class FingerprintResponse(val status: FingerprintStatus, val fingerprintId: UUID?)
    enum class FingerprintStatus { SUCCESS, CANCELLED, FAILURE }
}