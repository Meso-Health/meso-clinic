package org.watsi.device.managers

import android.content.Intent
import android.support.v4.app.Fragment
import java.util.UUID

interface FingerprintManager {
    fun captureFingerprint(memberId: String, fragment: Fragment, requestCode: Int): Boolean
    fun verifyFingerprint(guid: String, fragment: Fragment, requestCode: Int): Boolean
    fun parseResponseForRegistration(resultCode: Int, data: Intent?): FingerprintRequestResponse
    fun parseResponseForVerification(resultCode: Int, data: Intent?): FingerprintVerificationResponse

    data class FingerprintRequestResponse(
            val status: FingerprintStatus,
            val fingerprintId: UUID? = null)
    data class FingerprintVerificationResponse(
            val status: FingerprintStatus,
            val confidence: Float? = null,
            val tier: String? = null,
            val badScan: Boolean? = null)
    enum class FingerprintStatus { SUCCESS, CANCELLED, FAILURE }
}